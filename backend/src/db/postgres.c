#include "postgres.h"
#include "../utils/logger.h"

#include <libpq-fe.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static PGconn* conn = NULL;

bool db_connect(const db_config_t config) {
    char connection_info[512];

    snprintf(connection_info, sizeof(connection_info),
             "host=%s port=%d dbname=%s user=%s password=%s",
             config.host, config.port, config.dbname, config.user, config.password);

    conn = PQconnectdb(connection_info);

    if (PQstatus(conn) != CONNECTION_OK) {
        log_error("Connection to database failed: %s", PQerrorMessage(conn));
        PQfinish(conn);
        conn = NULL;
        return false;
    }

    log_info("Connected to PostgreSQL database");
    return true;
}

void db_disconnect(void) {
    if (conn) {
        PQfinish(conn);
        conn = NULL;
        log_info("Disconnected from PostgreSQL database");
    }
}

PGresult* db_execute_query(const char* query) {
    if (!conn) {
        log_error("Database connection not established");
        return NULL;
    }

    PGresult* res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK && PQresultStatus(res) != PGRES_COMMAND_OK) {
        log_error("Query execution failed: %s", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    return res;
}

void db_free_result(PGresult* result) {
    if (result) {
        PQclear(result);
    }
}
