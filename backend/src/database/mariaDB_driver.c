#include <stdlib.h>
#include <string.h>
#include <mariadb/mysql.h>
#include "database.h"

int mariadb_connectivity(DBHandle* handle, const DBParams* parameters) {
    MYSQL* connection = mysql_init(NULL);
    if(!connection) {
        //TODO log error
        return -1;
    }

    const unsigned int timeout = 10;
    mysql_options(connection, MYSQL_OPT_CONNECT_TIMEOUT, &timeout);

    if(!mysql_real_connect(
        connection,
        parameters->host,
        parameters->username,
        parameters->password,
        parameters->database_name,
        atoi(parameters->port),
        NULL,
        0
    )) {
        //TODO log error
        mysql_close(connection);
        return -1;
    }
    handle->connection = connection;
    return 0;
}

int mariadb_execute(DBHandle* handle, const char* query) {
    MYSQL* connection = (MYSQL*)handle->connection;
    if(mysql_real_query(connection, query, strlen(query))) {
        //TODO log error
        return -1;
    }
    return 0;
}

DBResult* mariadb_query(DBHandle* handle, const char* query) {
    MYSQL* connection = (MYSQL*)handle->connection;
    if(mysql_real_query(connection, query, strlen(query))) {
        //TODO log error
        return NULL;
    }

    MYSQL_RES* result = mysql_store_result(connection);
    if(!result) {
        //TODO log error
        return NULL;
    }

    DBResult* db_result = (DBResult*)malloc(sizeof(DBResult));
    db_result->data = result;
    db_result->row_count = mysql_num_rows(result);
    db_result->column_count = mysql_num_fields(result);

    return db_result;
}

const char* mariadb_get_cell(DBHandle* handle, const int row, const int column) {
    MYSQL_RES *res = (MYSQL_RES *)handle->result->data;
    mysql_data_seek(res, row);
    // ReSharper disable once CppLocalVariableMayBeConst
    MYSQL_ROW mysql_row = mysql_fetch_row(res);

    if (!mysql_row || column >= handle->result->column_count) {
        return NULL;
    }
    return mysql_row[column] ? mysql_row[column] : "NULL";
}

void mariadb_free_result(DBResult *result) {
    if (result) {
        mysql_free_result((MYSQL_RES *)result->data);
        free(result);
    }
}

int mariadb_disconnect(DBHandle *handle) {
    if (handle->connection) {
        mysql_close((MYSQL *)handle->connection);
        handle->connection = NULL;
    }
    return 0;
}

const DBDriver mariadb_driver = {
    .connect = mariadb_connectivity,
    .execute = mariadb_execute,
    .query = mariadb_query,
    .get_cell = mariadb_get_cell,
    .free_result = mariadb_free_result,
    .disconnect = mariadb_disconnect
};