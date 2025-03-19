#include "database.h"
#include <jansson.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

extern const DBDriver postgres_driver;
extern const DBDriver mariadb_driver;

static const DBDriver* get_driver(const char* db_type) {
    switch(db_type) {
        case "postgres":
            return &postgres_driver;
        case "mariadb":
            return &mariadb_driver;
        default:
            return NULL;
    }
}

DBHandle* db_open(const char* config_path) {
    json_error_t error;
    json_t* root = json_load_file(config_path, 0, &error);
    if(!root) {
        //TODO log error.text, error.line
        return NULL;
    }

    const char* database_type = json_string_value(json_object_get(root, "database_type"));
    const char* host = json_string_value(json_object_get(root, "host"));
    const char* port = json_string_value(json_object_get(root, "port"));
    const char* database_name = json_string_value(json_object_get(root, "database_name"));
    const char* username = json_string_value(json_object_get(root, "username"));
    const char* password = json_string_value(json_object_get(root, "password"));

    if(!database_type || !host || !port || !database_name || !username || !password) {
        //TODO log error
        return NULL;
    }

    const DBDriver* driver = get_driver(database_type);
    if(!driver) {
        //TODO log error
        json_decref(root);
        return NULL;
    }

    const DBParams params = {
        .host = host,
        .port = port, //TODO check if this is correct - long long should not be used for a port numer (max 65535)
        .database_name = database_name,
        .username = username,
        .password = password
    };

    DBHandle* handle = calloc(1, sizeof(DBHandle));
    if(driver->connect(handle, &params) != 0) {
        //TODO log error
        free(handle);
        handle = NULL;
    }

    json_decref(root);
    return handle;
}