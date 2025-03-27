#include "search_service.h"
#include <jansson.h>
#include <string.h>
#include "db/postgres.h"
#include "utils/json.h"
#include "utils/logger.h"

char* search_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if (!deSerialized) {
        log_error("Invalid JSON: %s", error.text);
        return "{\"status\":\"error\",\"message\":\"Invalid JSON\"}";
    }

    const char* titolo = json_string_value(json_object_get(deSerialized, "titolo"));
    const char* genere = json_string_value(json_object_get(deSerialized, "genere"));
    const char* regista = json_string_value(json_object_get(deSerialized, "regista"));
    const int anno = json_integer_value(json_object_get(deSerialized, "anno"));
    const int durata = json_integer_value(json_object_get(deSerialized, "durata"));

    char query[2048];
    snprintf(query, sizeof(query),
        "SELECT film_id, titolo, genere, regista, anno, durata "
        "FROM films "
        "WHERE 1=1"
        "%s%s%s"
        "%s%s%s"
        "%s%s%s"
        "%s%s%s"
        "%s%s%s",
        titolo ? " AND titolo LIKE '%" : "",
        titolo ? titolo : "",
        titolo ? "%'" : "",
        genere ? " AND genere LIKE '%" : "",
        genere ? genere : "",
        genere ? "%'" : "",
        regista ? " AND regista LIKE '%" : "",
        regista ? regista : "",
        regista ? "%'" : "",
        anno ? " AND anno = '%" : "",
        anno ? anno : "",
        anno ? "%'" : "",
        durata ? " AND durata = '%" : "",
        durata ? durata : "",
        durata ? "%'" : "");


    PGresult* result = db_execute_query(query);
    if (!result) {
        log_error("Error executing query");
        json_decref(deSerialized);
        return json_response_error("Errore nell'esecuzione della query");
    }

    json_t* response = json_object();
    json_object_set_new(response, "status", json_string("success"));
    json_t* films = json_array();

    const int rows = PQntuples(result);
    for (int i = 0; i < rows; i++) {
        json_t* film = json_object();
        json_object_set_new(film, "id", json_integer(atoi(PQgetvalue(result, i, 0))));
        json_object_set_new(film, "titolo", json_string(PQgetvalue(result, i, 1)));
        json_object_set_new(film, "genere", json_string(PQgetvalue(result, i, 2)));
        json_object_set_new(film, "regista", json_string(PQgetvalue(result, i, 3)));
        json_object_set_new(film, "release_year", json_integer(atoi(PQgetvalue(result, i, 4))));
        json_array_append_new(films, film);
    }

    json_object_set_new(response, "films", films);
    db_free_result(result);
    json_decref(deSerialized);

    char* response_str = json_dumps(response, 0);
    json_decref(response);
    return response_str;
}