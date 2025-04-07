#include "postgres.h"
#include "../utils/logger.h"

#include <libpq-fe.h>
#include <stdio.h>
#include <string.h>

static PGconn* conn = NULL;

bool db_connect(const db_config_t config) {
    char connection_info[512];

    snprintf(connection_info, sizeof(connection_info),
             "host=%s port=%d dbname=%s user=%s password=%s",
             config.host, config.port, config.dbname, config.user, config.password);

    conn = PQconnectdb(connection_info);

    if (PQstatus(conn) != CONNECTION_OK) {
        log_error("Connessione al database fallita: %s", PQerrorMessage(conn));
        PQfinish(conn);
        conn = NULL;
        return false;
    }

    log_info("Connesso al database PostgreSQL");
    return true;
}

void db_disconnect(void) {
    if (conn) {
        PQfinish(conn);
        conn = NULL;
        log_info("Disconnesso dal database PostgreSQL");
    }
}

PGresult* db_execute_query(const char* query, const int param_count, const char* params[]) {
    if (!conn) {
        log_error("Connessione al database non stabilita");
        return NULL;
    }

    PGresult* res = PQexecParams(conn, query, param_count, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK && PQresultStatus(res) != PGRES_COMMAND_OK) {
        log_error("Esecuzione della query fallita: %s", PQerrorMessage(conn));
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

bool db_begin_transaction() {
    if (!conn) {
        log_error("Connessione al database non stabilita");
        return false;
    }

    PGresult* res = PQexec(conn, "BEGIN");
    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        log_error("Impossibile iniziare la transazione: %s", PQerrorMessage(conn));
        PQclear(res);
        return false;
    }

    PQclear(res);
    return true;
}

bool db_rollback_transaction(void) {
    if (!conn) {
        log_error("Connessione al database non stabilita");
        return false;
    }

    PGresult* res = PQexec(conn, "ROLLBACK");
    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        log_error("Impossibile eseguire rollback della transazione: %s", PQerrorMessage(conn));
        PQclear(res);
        return false;
    }

    PQclear(res);
    return true;
}

bool db_commit_transaction(void) {
    if (!conn) {
        log_error("Connessione al database non stabilita");
        return false;
    }

    PGresult* res = PQexec(conn, "COMMIT");
    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        log_error("Impossibile eseguire commit della transazione: %s", PQerrorMessage(conn));
        PQclear(res);
        return false;
    }

    PQclear(res);
    return true;
}