#include "json.h"
#include "logger.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Crea una risposta JSON di successo
char* json_response_success(const char* message) {
    json_t* response = json_object();
    json_object_set_new(response, "status", json_string("success"));

    if (message) {
        json_object_set_new(response, "message", json_string(message));
    }

    char* response_str = json_dumps(response, JSON_COMPACT);
    json_decref(response);

    return response_str;
}

// Crea una risposta JSON di errore
char* json_response_error(const char* message) {
    json_t* response = json_object();
    json_object_set_new(response, "status", json_string("error"));

    if (message) {
        json_object_set_new(response, "message", json_string(message));
    }

    char* response_str = json_dumps(response, JSON_COMPACT);
    json_decref(response);

    return response_str;
}

// Serializza un JSON in una stringa, con gestione degli errori
char* json_dumps_safe(json_t* json) {
    if (!json) {
        log_error("Attempt to dump NULL JSON object");
        return NULL;
    }

    char* result = json_dumps(json, JSON_COMPACT);
    if (!result) {
        log_error("Failed to serialize JSON");
    }

    return result;
}