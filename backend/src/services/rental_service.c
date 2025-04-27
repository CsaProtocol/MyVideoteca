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
    const json_t* films_array = json_object_get(deSerialized, "films");

    if (user_id <= 0 || !json_is_array(films_array) || json_array_size(films_array) == 0) {
        log_error("User ID non valido o array film non presente/vuoto");
        json_decref(deSerialized);
        return json_response_error("User ID non valido o array film non presente/vuoto");
    }

    json_t* successful_rentals = json_array();
    json_t* failed_rentals = json_array();

    size_t index;
    const json_t* film_json;
    json_array_foreach(films_array, index, film_json) {
        const int film_id = json_integer_value(film_json);
        json_t* rental_result = json_object();
        json_object_set_new(rental_result, "film_id", json_integer(film_id));

        if (film_id <= 0) {
            json_object_set_new(rental_result, "error", json_string("Film ID non valido"));
            json_array_append_new(failed_rentals, rental_result);
            continue;
        }

        const char* query_check ="SELECT numero_copie_disponibili FROM Film WHERE film_id = $1;";

        char* params_check[1] = {NULL};
        params_check[0] = malloc(16);
        sprintf(params_check[0], "%d", film_id);

        PGresult* result = db_execute_query(query_check, 1, (const char**) params_check);
        free((void*)params_check[0]);

        if (!result || PQntuples(result) == 0) {
            json_object_set_new(rental_result, "error", json_string("Errore database"));
            json_array_append_new(failed_rentals, rental_result);
            continue;
        }

        const int copie_disponibili = atoi(PQgetvalue(result, 0, 0));
        db_free_result(result);

        if (copie_disponibili <= 0) {
            json_object_set_new(rental_result, "error", json_string("Film non disponibile"));
            json_array_append_new(failed_rentals, rental_result);
            continue;
        }

        const char* query_insert =
            "INSERT INTO Noleggio (utente_id, film_id) "
            "VALUES ($1, $2) RETURNING noleggio_id;";

        char* params_insert[2];
        params_insert[0] = malloc(16);
        sprintf(params_insert[0], "%d", user_id);
        params_insert[1] = malloc(16);
        sprintf(params_insert[1], "%d", film_id);

        result = db_execute_query(query_insert, 2, (const char**) params_insert);
        free(params_insert[0]);
        free(params_insert[1]);

        if(!result) {
            json_object_set_new(rental_result, "error", json_string("Errore creazione noleggio"));
            json_array_append_new(failed_rentals, rental_result);
            continue;
        }

        const int rental_id = atoi(PQgetvalue(result, 0, 0));
        db_free_result(result);

        json_object_set_new(rental_result, "noleggio_id", json_integer(rental_id));
        json_array_append_new(successful_rentals, rental_result);
    }
    json_decref(deSerialized);

    json_t* response = json_object();
    json_object_set_new(response, "status", json_string("success"));
    json_object_set_new(response, "successful_rentals", successful_rentals);
    json_object_set_new(response, "failed_rentals", failed_rentals);

    char* response_str = json_dumps(response, 0);
    json_decref(response);
    return response_str;
}
