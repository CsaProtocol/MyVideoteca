#include "film.h"
#include "../db/postgres.h"
#include "../utils/logger.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

film_t* film_create(const char* title, const char* director, const char* genre,
                   const int year, const int total_copies) {
    char query[1024];
    snprintf(query, sizeof(query),
             "INSERT INTO Film(titolo, regista, genere, year, numero_copie, numero_copie_disponibili) "
             "VALUES ($1, $2, $3, $4, $5, $6) RETURNING *");

    const char* params[6] = {title, director, genre, (char*)&year, (char*)&total_copies, (char*)&total_copies};
    PGresult* result = db_execute_query(query, 6, params);
    if (!result) {
        log_error("Creazione del film fallita");
        return NULL;
    }

    film_t* film = NULL;

    if (PQntuples(result) == 0) {
        log_error("Inserimento fallito. Controlla i log per maggiori informazioni.");
    } else {
        film = (film_t*)malloc(sizeof(film_t));
        if (film) {
            film->id = atoi(PQgetvalue(result, 0, 0));
            strncpy(film->title, PQgetvalue(result, 0, 1), sizeof(film->title) - 1);
            film->title[sizeof(film->title) - 1] = '\0';

            strncpy(film->director, PQgetvalue(result, 0, 2), sizeof(film->director) - 1);
            film->director[sizeof(film->director) - 1] = '\0';

            strncpy(film->genre, PQgetvalue(result, 0, 3), sizeof(film->genre) - 1);
            film->genre[sizeof(film->genre) - 1] = '\0';

            film->year = atoi(PQgetvalue(result, 0, 4));
            film->total_copies = atoi(PQgetvalue(result, 0, 5));
            film->available_copies = atoi(PQgetvalue(result, 0, 6));
            film->popularity_score = 0;  // Assume initial popularity score is zero
            film->created_at = time(NULL);

            log_info("Film \"%s\" creato con successo", film->title);
        } else {
            log_error("Allocazione di memoria per il film fallita");
        }
    }

    db_free_result(result);
    return film;
}