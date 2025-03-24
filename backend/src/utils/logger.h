#ifndef LOGGER_H
#define LOGGER_H

typedef enum {
    LOG_DEBUG,
    LOG_INFO,
    LOG_WARNING,
    LOG_ERROR
} log_level_t;

void init_logger(const char* log_file_path, log_level_t min_level);
void log_debug(const char* format, ...);
void log_info(const char* format, ...);
void log_warning(const char* format, ...);
void log_error(const char* format, ...);
void close_logger(void);

#endif // LOGGER_H