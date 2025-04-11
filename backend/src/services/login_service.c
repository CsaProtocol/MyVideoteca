#include "login_service.h"
#include <jansson.h>
#include <string.h>

#include "../auth/auth.h"
#include "db/postgres.h"
#include "utils/json.h"
#include "utils/logger.h"

char* login_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if (!deSerialized) {
        log_error("JSON non valido: %s", error.text);
        return json_response_error("JSON non valido");
    }

    const char* email = json_string_value(json_object_get(deSerialized, "email"));
    const char* password = json_string_value(json_object_get(deSerialized, "password"));

    if (!login(email, password)) {
        log_error("Login fallito");
        json_decref(deSerialized);
        return json_response_error("Login fallito");
    }

    const char* query1 = "SELECT id FROM Utente WHERE email = $1;";
    const char* params1[1] = {email};
    log_info("Esecuzione della query per ottenere l'ID dell'utente con email: %s", email);
    PGresult* result = db_execute_query(query1, 1, params1);
    if (!result) {
        log_error("Errore nell'esecuzione della query");
        json_decref(deSerialized);
        return json_response_error("Errore nell'esecuzione della query");
    }

    char* id = PQgetvalue(result, 0, 0);
    db_free_result(result);

    const char* query2 = "SELECT aggiorna_flag_utente(CAST($1 AS INT));";
    const char* params2[1] = {id};
    log_info("Esecuzione della query per aggiornare il flag dell'utente con ID: %d", id);
    result = db_execute_query(query2, 1, params2);
    if (!result) {
        log_error("Errore nell'esecuzione della query");
        json_decref(deSerialized);
        return json_response_error("Errore nell'esecuzione della query");
    }
    db_free_result(result);

    const char* query3 =
        "SELECT id, numero_film_non_restituiti, get_massimo_noleggi() AS max_noleggi, film_non_restituiti "
        "FROM utente "
        "FULL JOIN (SELECT COUNT(*) AS numero_film_non_restituiti, utente_id FROM Noleggio "
        "WHERE restituito = 'FALSE' GROUP BY utente_id) ON Utente.id = utente_id WHERE email = $1;";

    const char* params3[1] = {email};
    log_info("Esecuzione della query per ottenere i dettagli dell'utente con email: %s", email);
    result = db_execute_query(query3, 1, params3);
    if (!result) {
        log_error("Errore nell'esecuzione della query");
        json_decref(deSerialized);
        return json_response_error("Errore nell'esecuzione della query");
    }

    json_t* response = json_object();
    json_object_set_new(response, "status", json_string("success"));
    json_object_set_new(response, "message", json_string("Login effettuato con successo"));
    json_object_set_new(response, "id", json_integer(atoi(id)));
    json_object_set_new(response, "numero_film_non_restituiti", json_integer(atoi(PQgetvalue(result, 0, 1))));
    json_object_set_new(response, "max_noleggi", json_integer(atoi(PQgetvalue(result, 0, 2))));
    json_object_set_new(response, "film_non_restituiti", json_string(PQgetvalue(result, 0, 3)));

    db_free_result(result);
    json_decref(deSerialized);

    char* response_str = json_dumps(response, 0);
    json_decref(response);
    return response_str;
}