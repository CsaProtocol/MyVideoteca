#include "signup_service.h"
#include <jansson.h>
#include <string.h>
#include "../auth/auth.h"

#include "utils/json.h"
#include "utils/logger.h"

char* signup_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if (!deSerialized) {
        log_error("Invalid JSON: %s", error.text);
        return "{\"status\":\"error\",\"message\":\"Invalid JSON\"}";
    }

    const char* email = json_string_value(json_object_get(deSerialized, "email"));
    const char* password = json_string_value(json_object_get(deSerialized, "password"));
    const char* nome = json_string_value(json_object_get(deSerialized, "nome"));
    const char* cognome = json_string_value(json_object_get(deSerialized, "cognome"));

    if (!signup(nome, cognome, email, password)) {
        log_error("Signup failed");
        json_decref(deSerialized);
        return "{\"status\":\"error\",\"message\":\"Signup failed\"}";
    }

    json_decref(deSerialized);
    return json_response_success("Signup successful");
}