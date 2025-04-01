#include "search_service.h"
#include <jansson.h>
#include <string.h>
#include <stdio.h>
#include "db/postgres.h"
#include "utils/json.h"
#include "utils/logger.h"

char* search_service(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0, &error);

    if (!deSerialized) {
        log_error("JSON non valido: %s", error.text);
        return json_response_error("JSON non valido");
    }

    const char* titolo = json_string_value(json_object_get(deSerialized, "titolo"));
    const char* genere = json_string_value(json_object_get(deSerialized, "genere"));
    const char* regista = json_string_value(json_object_get(deSerialized, "regista"));
    const int anno = json_integer_value(json_object_get(deSerialized, "anno"));
    const int durata_min = json_integer_value(json_object_get(deSerialized, "durata_min"));
    const int durata_max = json_integer_value(json_object_get(deSerialized, "durata_max"));

    char where_clause[1024] = "";
    char* params[6] = {NULL};
    int param_count = 0;

    if (titolo) {
        char titolo_pattern[256];
        snprintf(titolo_pattern, sizeof(titolo_pattern), "%%%s%%", titolo);
        strcat(where_clause, " AND titolo LIKE $1");
        params[param_count++] = strdup(titolo_pattern);
    }

    if (genere) {
        char genere_pattern[256];
        snprintf(genere_pattern, sizeof(genere_pattern), "%%%s%%", genere);
        sprintf(where_clause + strlen(where_clause), " AND genere LIKE $%d", param_count + 1);
        params[param_count++] = strdup(genere_pattern);
    }

    if (regista) {
        char regista_pattern[256];
        snprintf(regista_pattern, sizeof(regista_pattern), "%%%s%%", regista);
        sprintf(where_clause + strlen(where_clause), " AND regista LIKE $%d", param_count + 1);
        params[param_count++] = strdup(regista_pattern);
    }

    if (anno > 0) {
        sprintf(where_clause + strlen(where_clause), " AND anno = $%d", param_count + 1);
        char* anno_str = malloc(16);
        sprintf(anno_str, "%d", anno);
        params[param_count++] = anno_str;
    }

    if (durata_min > 0 && durata_max > 0) {
        sprintf(where_clause + strlen(where_clause), " AND durata BETWEEN $%d AND $%d", param_count + 1, param_count + 1);
        char* durata_str = malloc(16);
        sprintf(durata_str, "%d", durata_min);
        params[param_count++] = durata_str;
        char* durata_max_str = malloc(16);
        sprintf(durata_max_str, "%d", durata_max);
        params[param_count++] = durata_max_str;
    }

    char query[2048];
    snprintf(query, sizeof(query),
        "SELECT film_id, titolo, genere, regista, anno, durata, descrizione, numero_copie, numero_copie_disponibili "
        "FROM Film "
        "WHERE 1=1 %s", where_clause);

    PGresult* result = db_execute_query(query, param_count, (const char**)params);

    for (int i = 0; i < param_count; i++) {
        free(params[i]);
    }

    if (!result) {
        log_error("Errore nell'esecuzione della query");
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
        json_object_set_new(film, "anno", json_integer(atoi(PQgetvalue(result, i, 4))));
        json_object_set_new(film, "durata", json_integer(atoi(PQgetvalue(result, i, 5))));
        json_object_set_new(film, "descrizione", json_string(PQgetvalue(result, i, 6)));
        json_object_set_new(film, "numero_copie", json_integer(atoi(PQgetvalue(result, i, 7))));
        json_object_set_new(film, "numero_copie_disponibili", json_integer(atoi(PQgetvalue(result, i, 8))));
        json_array_append_new(films, film);
    }

    json_object_set_new(response, "films", films);
    db_free_result(result);
    json_decref(deSerialized);

    char* response_str = json_dumps(response, 0);
    json_decref(response);
    return response_str;
}