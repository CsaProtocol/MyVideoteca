#include <libpq-fe.h>
#include <stdlib.h>
#include <string.h>

#include "database.h"


int postgres_connect(DBHandle* handle, const DBParams* parameters) {
    char conn_string[512];
    snprintf(
        conn_string, sizeof(conn_string),
        "host=%s port=%s dbname=%s user=%s password=%s",
        parameters->host, parameters->port, parameters->database_name,
        parameters->username, parameters->password
    );

    PGconn* connection = PQconnectdb(conn_string);
    if(PQstatus(connection) != CONNECTION_OK) {
        handle->error_code = PQstatus(connection);
        //TODO log( PQerrorMessage(connection) )
        PQfinish(connection);
        return -1;
    }
    handle->connection = connection;
    return 0;
}

int postgres_execute(DBHandle* handle, const char* query) {
    PGconn* connection = (PGconn *)handle->connection;
    PGresult* result = PQexec(connection, query);
    const ExecStatusType status = PQresultStatus(result);
    if(status != PGRES_COMMAND_OK && status != PGRES_TUPLES_OK) {
        handle->error_code = status;
        //TODO log( PQerrorMessage(connection) )
        PQclear(result);
        return -1;
    }
    PQclear(result);
    return 0;
}

DBResult* postgres_query(DBHandle* handle, const char *query) {
    PGconn* connection = (PGconn *)handle->connection;
    PGresult* result = PQexec(connection, query);

    if(PQresultStatus(result) != PGRES_TUPLES_OK) {
        handle->error_code = PQresultStatus(result);
        //TODO log( PQerrorMessage(connection) )
        PQclear(result);
        return NULL;
    }

    DBResult* db_result = malloc(sizeof(DBResult));
    db_result->data = result;
    db_result->row_count = PQntuples(result);
    db_result->column_count = PQnfields(result);
    return db_result;
}

// ReSharper disable once CppParameterMayBeConstPtrOrRef
const char* postgres_get_cell(DBHandle* handle, const int row, const int column) {
    PGresult const* result = (PGresult *)handle->result->data;
    return PQgetvalue(result, row, column);
}

void postgres_free_result(DBResult* result) {
    if(result) {
        PQclear((PGresult *)result->data);
        free(result);
    }
}

int postgres_disconnect(DBHandle* handle) {
    PGconn* connection = (PGconn *)handle->connection;
    PQfinish(connection);
    handle->connection = NULL;
    return 0;
}

const DBDriver postgres_driver = {
    .connect = postgres_connect,
    .execute = postgres_execute,
    .query = postgres_query,
    .get_cell = postgres_get_cell,
    .free_result = postgres_free_result,
    .disconnect = postgres_disconnect
};