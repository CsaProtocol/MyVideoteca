#include "all_rentals_service.h"

#include <string.h>

#include "db/postgres.h"
#include "utils/json.h"
#include "utils/logger.h"

char* all_rentals_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if(!deSerialized) {
        log_error("JSON non valido: %s", error.text);
        return json_response_error("JSON non valido");
    }

    const char* userid = json_string_value(json_object_get(deSerialized, "userid"));

    const char query =
        "SELECT noleggio_id, film_id, data_noleggio, data_scadenza, restituito  "
        "FROM noleggi "
        "WHERE id_utente = $1 AND restituito = FALSE;";

    const char* params[1] = {userid};
    PGresult* result = db_execute_query(query, 1, params);
    if(!result) {
        log_error("Errore nell'esecuzione della query");
        json_decref(deSerialized);
        return json_response_error("Errore nell'esecuzione della query");
    }

    json_t* rentals = json_array();
    for(int i = 0; i < PQntuples(result); i++) {
        json_t* rental = json_object();
        json_object_set_new(rental, "noleggio_id", json_integer(atoi(PQgetvalue(result, i, 0))));
        json_object_set_new(rental, "film_id", json_integer(atoi(PQgetvalue(result, i, 1))));
        json_object_set_new(rental, "data_noleggio", json_string(PQgetvalue(result, i, 2)));
        json_object_set_new(rental, "data_scadenza", json_string(PQgetvalue(result, i, 3)));
        json_array_append_new(rentals, rental);
    }
    db_free_result(result);
    json_decref(deSerialized);

    char* response_str = json_dumps(rentals, 0);
    json_decref(rentals);
    return response_str;
}
