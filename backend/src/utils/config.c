#include "config.h"
#include <jansson.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "logger.h"

static const db_config_t default_db_config = {
    .host = "localhost",
    .port = 5432,
    .dbname = "videoteca",
    .user = "postgres",
    .password = "password"
};

static const server_config_t default_server_config = {
    .port = 8080,
    .backlog = 10,
    .max_connections = 100
};

static bool file_exists(const char* file_path) {
    FILE* file = fopen(file_path, "r");
    if (file) {
        fclose(file);
        return true;
    }
    return false;
}

bool save_config(const char* file_path, const db_config_t* db_config, const server_config_t* server_config) {
    json_t* root = json_object();
    if (!root) {
        log_error("Failed to create JSON object");
        return false;
    }

    json_t* db = json_object();
    json_object_set_new(root, "database", db);
    json_object_set_new(db, "host", json_string(db_config->host));
    json_object_set_new(db, "port", json_integer(db_config->port));
    json_object_set_new(db, "dbname", json_string(db_config->dbname));
    json_object_set_new(db, "user", json_string(db_config->user));
    json_object_set_new(db, "password", json_string(db_config->password));

    json_t* server = json_object();
    json_object_set_new(root, "server", server);
    json_object_set_new(server, "port", json_integer(server_config->port));
    json_object_set_new(server, "backlog", json_integer(server_config->backlog));
    json_object_set_new(server, "max_connections", json_integer(server_config->max_connections));

    const int result = json_dump_file(root, file_path, JSON_INDENT(4));
    json_decref(root);

    if (result != 0) {
        log_error("Failed to write configuration to file");
        return false;
    }

    log_info("Configuration saved to %s", file_path);
    return true;
}

bool load_config(const char* file_path, db_config_t* db_config, server_config_t* server_config) {
    if (!file_exists(file_path)) {
        log_info("Configuration file doesn't exist, creating with default values");

        *db_config = default_db_config;
        *server_config = default_server_config;

        return save_config(file_path, db_config, server_config);
    }

    json_error_t error;
    json_t* root = json_load_file(file_path, 0, &error);
    if (!root) {
        log_error("Failed to parse configuration JSON: %s (line: %d, col: %d)", 
                  error.text, error.line, error.column);
        return false;
    }

    *db_config = default_db_config;
    *server_config = default_server_config;

    const json_t* db = json_object_get(root, "database");
    if (json_is_object(db)) {
        const json_t* host = json_object_get(db, "host");
        if (json_is_string(host)) {
            strncpy((char*)db_config->host, json_string_value(host), sizeof(db_config->host) - 1);
        }

        const json_t* port = json_object_get(db, "port");
        if (json_is_integer(port)) {
            db_config->port = json_integer_value(port);
        }

        const json_t* dbname = json_object_get(db, "dbname");
        if (json_is_string(dbname)) {
            strncpy((char*)db_config->dbname, json_string_value(dbname), sizeof(db_config->dbname) - 1);
        }

        const json_t* user = json_object_get(db, "user");
        if (json_is_string(user)) {
            strncpy((char*)db_config->user, json_string_value(user), sizeof(db_config->user) - 1);
        }

        const json_t* password = json_object_get(db, "password");
        if (json_is_string(password)) {
            strncpy((char*)db_config->password, json_string_value(password), sizeof(db_config->password) - 1);
        }
    }

    const json_t* server = json_object_get(root, "server");
    if (json_is_object(server)) {
        const json_t* port = json_object_get(server, "port");
        if (json_is_integer(port)) {
            server_config->port = json_integer_value(port);
        }

        const json_t* backlog = json_object_get(server, "backlog");
        if (json_is_integer(backlog)) {
            server_config->backlog = json_integer_value(backlog);
        }

        const json_t* max_connections = json_object_get(server, "max_connections");
        if (json_is_integer(max_connections)) {
            server_config->max_connections = json_integer_value(max_connections);
        }
    }

    json_decref(root);
    log_info("Configuration loaded from %s", file_path);
    return true;
}