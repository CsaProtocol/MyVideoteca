#include "return_service.h"
#include <jansson.h>
#include <string.h>
#include "db/postgres.h"
#include "utils/json.h"
#include "utils/logger.h"

char* return_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if (!deSerialized) {
        log_error("JSON non valido: %s", error.text);
        return json_response_error("JSON non valido");
    }

    const int rental_id = json_integer_value(json_object_get(deSerialized, "rental_id"));
    const int user_id = json_integer_value(json_object_get(deSerialized, "user_id"));
    const int film_id = json_integer_value(json_object_get(deSerialized, "film_id"));

    PGresult* result;
    const char* query;
    const char* params[2];

    if (rental_id > 0) {
        query = "UPDATE Noleggio "
                "SET data_restituzione = CURRENT_DATE "
                "WHERE id = $1 AND data_restituzione IS NULL "
                "RETURNING id;";
        params[0] = (char*)&rental_id;
        result = db_execute_query(query, 1, params);
    } else if (user_id > 0 && film_id > 0) {
        query = "UPDATE Noleggio "
                "SET data_restituzione = CURRENT_DATE "
                "WHERE id_utente = $1 AND id_film = $2 AND data_restituzione IS NULL "
                "RETURNING id;";
        params[0] = (char*)&user_id;
        params[1] = (char*)&film_id;
        result = db_execute_query(query, 2, params);
    } else {
        json_decref(deSerialized);
        return json_response_error("Dati per la restituzione non validi");
    }

    if (!result) {
        log_error("Errore nell'esecuzione della query di restituzione");
        json_decref(deSerialized);
        return json_response_error("Errore nella elaborazione della restituzione");
    }

    const int rows = PQntuples(result);
    if (rows == 0) {
        db_free_result(result);
        json_decref(deSerialized);
        return json_response_error("Nessun noleggio attivo trovato");
    }

    const int updated_rental_id = atoi(PQgetvalue(result, 0, 0));
    db_free_result(result);
    json_decref(deSerialized);

    json_t* response = json_object();
    json_object_set_new(response, "status", json_string("success"));
    json_object_set_new(response, "message", json_string("Film restituito con successo"));
    json_object_set_new(response, "noleggio_id", json_integer(updated_rental_id));

    char* response_str = json_dumps(response, 0);
    json_decref(response);
    return response_str;
}