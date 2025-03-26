#include "auth.h"

#include <stdbool.h>
#include <string.h>
#include <openssl/sha.h>

#include "db/postgres.h"
#include "utils/logger.h"

bool login(const char* email, const char* password) {
    unsigned char hash[SHA512_DIGEST_LENGTH];
    SHA512(password, strlen(password), hash);

    char query[1024];
    snprintf("SELECT * FROM users WHERE email = %s AND password = %s", email, hash);

    const PGresult* result = db_execute_query(query);

    if(result == NULL) {
        log_error("Error executing query");
        return false;
    }
    if (PQntuples(result) == 0) {
        return false;
    }
    return true;
}

bool signup(const char* nome, const char* cognome, const char* email, const char* password) {
    unsigned char hash[SHA512_DIGEST_LENGTH];
    SHA512(password, strlen(password), hash);

    char query[1024];
    snprintf("INSERT INTO users(nome, cognome, email, password) VALUES(%s, %s, %s, %s) RETURNING *",
        nome, cognome, email, hash);

    const PGresult* result = db_execute_query(query);

    if(result == NULL) {
        log_error("Error executing query");
        return false;
    }
    if (PQntuples(result) == 0) {
        return false;
    }
    return true;
}
