#include "json.h"
#include "logger.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char* json_response_success(const char* message) {
    json_t* response = json_object();
    if (!response) {
        log_error("Impossibile creare oggetto JSON");
        return json_response_error("Errore interno del server");
    }

    json_object_set_new(response, "status", json_string("success"));

    if (message) {
        json_object_set_new(response, "message", json_string(message));
    }

    char* response_str = json_dumps(response, JSON_COMPACT);
    json_decref(response);

    if (!response_str) {
        return json_response_error("Errore nella serializzazione JSON");
    }

    return response_str;
}

char* json_response_error(const char* message) {
    json_t* response = json_object();
    if (!response) {
        log_error("Impossibile creare oggetto JSON");
        return strdup("{\"status\":\"error\",\"message\":\"Errore interno del server\"}");
    }

    json_object_set_new(response, "status", json_string("error"));

    if (message) {
        json_object_set_new(response, "message", json_string(message));
    }

    char* response_str = json_dumps(response, JSON_COMPACT);
    json_decref(response);

    if (!response_str) {
        log_error("Impossibile serializzare risposta di errore");
        return strdup("{\"status\":\"error\",\"message\":\"Errore interno del server\"}");
    }

    return response_str;
}

char* json_dumps_safe(json_t* json) {
    if (!json) {
        log_error("Tentativo di serializzare un oggetto JSON NULL");
        return json_response_error("Oggetto JSON non valido");
    }

    char* result = json_dumps(json, JSON_COMPACT);
    if (!result) {
        log_error("Impossibile serializzare JSON");
        return json_response_error("Errore nella serializzazione JSON");
    }

    return result;
}