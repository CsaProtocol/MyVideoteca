#include "logger.h"

#include <pthread.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

static FILE* log_file = NULL;
static log_level_t current_min_level = LOG_INFO;
static pthread_mutex_t log_mutex = PTHREAD_MUTEX_INITIALIZER;

void init_logger(const char* log_file_path, const log_level_t min_level) {
    pthread_mutex_lock(&log_mutex);

    if (log_file) {
        fclose(log_file);
    }

    log_file = fopen(log_file_path, "a");
    if (!log_file) {
        fprintf(stderr, "Failed to open log file: %s\n", log_file_path);
        log_file = stderr;
    }

    current_min_level = min_level;
    pthread_mutex_unlock(&log_mutex);
}

static void log_message(const log_level_t level, const char* level_str, const char* format, const va_list args) {
    if (level < current_min_level) return;

    pthread_mutex_lock(&log_mutex);

    const time_t now = time(NULL);
    const struct tm* tm_info = localtime(&now);

    char timestamp[26];
    strftime(timestamp, sizeof(timestamp), "%Y-%m-%d %H:%M:%S", tm_info);

    const pthread_t thread_id = pthread_self();

    fprintf(log_file, "[%s] [%lu] [%s] ", timestamp, thread_id, level_str);
    vfprintf(log_file, format, args);
    fprintf(log_file, "\n");
    fflush(log_file);

    pthread_mutex_unlock(&log_mutex);
}

void log_debug(const char* format) {
    va_list args;
    va_start(args, format);
    log_message(LOG_DEBUG, "DEBUG", format, args);
    va_end(args);
}

void log_info(const char* format) {
    va_list args;
    va_start(args, format);
    log_message(LOG_INFO, "INFO", format, args);
    va_end(args);
}

void log_warning(const char* format) {
    va_list args;
    va_start(args, format);
    log_message(LOG_WARNING, "WARNING", format, args);
    va_end(args);
}

void log_error(const char* format) {
    va_list args;
    va_start(args, format);
    log_message(LOG_ERROR, "ERROR", format, args);
    va_end(args);
}

void close_logger(void) {
    pthread_mutex_lock(&log_mutex);

    if (log_file && log_file != stderr) {
        fclose(log_file);
        log_file = NULL;
    }

    pthread_mutex_unlock(&log_mutex);
}
