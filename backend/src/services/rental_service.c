#include "rental_service.h"
#include <jansson.h>
#include <string.h>
#include "db/postgres.h"
#include "utils/json.h"
#include "utils/logger.h"

char* rental_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if (!deSerialized) {
        log_error("JSON non valido: %s", error.text);
        return json_response_error("JSON non valido");
    }

    const int user_id = json_integer_value(json_object_get(deSerialized, "user_id"));
    const int film_id = json_integer_value(json_object_get(deSerialized, "film_id"));

    if (user_id <= 0 || film_id <= 0) {
        log_error("User ID o film ID non validi");
        json_decref(deSerialized);
        return json_response_error("User ID o film ID non validi");
    }

    const char* query1 =
        "SELECT EXISTS(SELECT 1 FROM films WHERE id = $1) AS film_exists, "
        "NOT EXISTS(SELECT 1 FROM noleggi WHERE id_film = $1 AND data_restituzione IS NULL) AS available;";

    const char* params1[1] = {(char*)&film_id};
    PGresult* result = db_execute_query(query1, 1, params1);
    if (!result) {
        log_error("Errore nel controllo della disponibilità del film");
        json_decref(deSerialized);
        return json_response_error("Errore nel controllo della disponibilità del film");
    }

    const int film_exists = strcmp(PQgetvalue(result, 0, 0), "t") == 0;
    const int available = strcmp(PQgetvalue(result, 0, 1), "t") == 0;
    db_free_result(result);

    if (!film_exists) {
        log_error("Film non trovato");
        json_decref(deSerialized);
        return json_response_error("Film non trovato");
    }

    if (!available) {
        log_error("Film non disponibile");
        json_decref(deSerialized);
        return json_response_error("Film non disponibile");
    }

    const char* query2 =
        "INSERT INTO noleggi (id_utente, id_film, data_noleggio) "
        "VALUES ($1, $2, CURRENT_DATE) RETURNING id;";

    const char* params2[2] = {(char*)&user_id, (char*)&film_id};
    result = db_execute_query(query2, 2, params2);
    if (!result) {
        log_error("Errore nella creazione di un noleggio");
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