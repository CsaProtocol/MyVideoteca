#include "rental_service.h"
#include <jansson.h>
#include <string.h>
#include "db/postgres.h"
#include "utils/logger.h"
#include "utils/json.h"

char* rental_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if (!deSerialized) {
        log_error("Invalid JSON: %s", error.text);
        return json_response_error("JSON non valido");
    }

    const int user_id = json_integer_value(json_object_get(deSerialized, "user_id"));
    const int film_id = json_integer_value(json_object_get(deSerialized, "film_id"));

    if (user_id <= 0 || film_id <= 0) {
        log_error("Invalid user_id or film_id");
        json_decref(deSerialized);
        return json_response_error("User ID o film ID non validi");
    }

    char query[1024];

    snprintf(query, sizeof(query),
        "SELECT EXISTS(SELECT 1 FROM films WHERE id = %d) AS film_exists, "
        "NOT EXISTS(SELECT 1 FROM noleggi WHERE id_film = %d AND data_restituzione IS NULL) AS available;",
        film_id, film_id);

    PGresult* result = db_execute_query(query);
    if (!result) {
        log_error("Error checking film availability");
        json_decref(deSerialized);
        return json_response_error("Errore nel controllo della disponibilità del film");
    }

    const int film_exists = strcmp(PQgetvalue(result, 0, 0), "t") == 0;
    const int available = strcmp(PQgetvalue(result, 0, 1), "t") == 0;
    db_free_result(result);

    if (!film_exists) {
        log_error("Film not found");
        json_decref(deSerialized);
        return json_response_error("Film non trovato");
    }

    if (!available) {
        log_error("Film not available");
        json_decref(deSerialized);
        return json_response_error("Film non disponibile");
    }

    snprintf(query, sizeof(query),
        "INSERT INTO noleggi (id_utente, id_film, data_noleggio) "
        "VALUES (%d, %d, CURRENT_DATE) RETURNING id;", user_id, film_id);

    result = db_execute_query(query);
    if (!result) {
        log_error("Error creating rental");
        json_decref(deSerialized);
        return json_response_error("Errore nella creazione di un noleggio");
    }

    const int rental_id = atoi(PQgetvalue(result, 0, 0));
    db_free_result(result);

    json_decref(deSerialized);

    json_t* response = json_object();
    json_object_set_new(response, "status", json_string("success"));
    json_object_set_new(response, "message", json_string("Noleggiato con successo"));
    json_object_set_new(response, "noleggio_id", json_integer(rental_id));

    char* response_str = json_dumps(response, 0);
    json_decref(response);
    return response_str;
}