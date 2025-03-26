#include "login_service.h"
#include <jansson.h>
#include <string.h>

#include "../auth/auth.h"
#include "db/postgres.h"
#include "utils/logger.h"

char* login_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if (!deSerialized) {
        log_error("Invalid JSON: %s", error.text);
        return "{\"status\":\"error\",\"message\":\"Invalid JSON\"}";
    }

    const char* email = json_string_value(json_object_get(deSerialized, "email"));
    const char* password = json_string_value(json_object_get(deSerialized, "password"));

    if (!login(email, password)) {
        log_error("Login failed");
        json_decref(deSerialized);
        return "{\"status\":\"error\",\"message\":\"Login failed\"}";
    }

    char query[1024];
    snprintf(query, sizeof(query), "SELECT id FROM utenti WHERE email = '%s';", email);

    PGresult* result = db_execute_query(query);
    if (!result) {
        log_error("Error executing query");
        json_decref(deSerialized);
        return "{\"status\":\"error\",\"message\":\"Error executing query\"}";
    }

    int id = atoi(PQgetvalue(result, 0, 0));
    db_free_result(result);

    snprintf(query, sizeof(query), "SELECT aggiorna_flag_utente(%d);", id);
    result = db_execute_query(query);
    if (!result) {
        log_error("Error executing query");
        json_decref(deSerialized);
        return "{\"status\":\"error\",\"message\":\"Error executing query\"}";
    }
    db_free_result(result);

    snprintf(query, sizeof(query),
        "SELECT id, numero_film_non_restituiti, get_massimo_noleggi() AS max_noleggi, film_non_restituiti "
        "FROM utenti "
        "FULL JOIN (SELECT COUNT(*) AS numero_film_non_restituiti, id_utente FROM noleggi WHERE data_restituzione IS NULL GROUP BY id_utente) "
        "ON utenti.id = id_utente WHERE email = '%s';", email);

    result = db_execute_query(query);
    if (!result) {
        log_error("Error executing query");
        json_decref(deSerialized);
        return "{\"status\":\"error\",\"message\":\"Error executing query\"}";
    }

    json_t* response = json_object();
    json_object_set_new(response, "status", json_string("success"));
    json_object_set_new(response, "message", json_string("Login successful"));
    json_object_set_new(response, "id", json_integer(id));
    json_object_set_new(response, "numero_film_non_restituiti", json_integer(atoi(PQgetvalue(result, 0, 1))));
    json_object_set_new(response, "max_noleggi", json_integer(atoi(PQgetvalue(result, 0, 2))));
    json_object_set_new(response, "film_non_restituiti", json_string(PQgetvalue(result, 0, 3)));

    db_free_result(result);
    json_decref(deSerialized);

    char* response_str = json_dumps(response, 0);
    json_decref(response);
    return response_str;
}