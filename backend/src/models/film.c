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
             "INSERT INTO films (title, director, genre, year, total_copies, available_copies) "
             "VALUES ('%s', '%s', '%s', %d, %d, %d) RETURNING *",
             title, director, genre, year, total_copies, total_copies);

    PGresult* result = db_execute_query(query);
    if (!result) {
        log_error("Failed to create film");
        return NULL;
    }

    film_t* film = NULL;

    if (PQntuples(result) == 0) {
        log_error("Insert failed. Check logs for more information.");
        //TODO - Mi serve ritornare film? --  film = funzione(res, 0);
    }

    db_free_result(result);
    return film;
}