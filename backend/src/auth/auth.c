#include "auth.h"
#include <argon2.h>
#include <string.h>
#include <openssl/rand.h>
#include "db/postgres.h"
#include "utils/logger.h"

bool login(const char* email, const char* password) {
    char query[256];
    snprintf(query, sizeof(query), "SELECT password FROM users WHERE email = $1");

    const char* params[1] = {email};
    PGresult* result = db_execute_query(query, 1, params);

    if (result == NULL) {
        log_error("Errore nell'esecuzione della query di login");
        return false;
    }

    if (PQntuples(result) == 0) {
        db_free_result(result);
        return false;
    }

    const char* stored_hash = PQgetvalue(result, 0, 0);
    const int verify_result = argon2i_verify(stored_hash, password, strlen(password));

    db_free_result(result);
    return (verify_result == ARGON2_OK);
}

int generate_salt(unsigned char* buffer, const int length) {
    if(RAND_bytes(buffer, length) != 1) {
        log_error("Impossibile generare byte casuali");
        return -1;
    }
    return 0;
}

bool signup(const char* nome, const char* cognome, const char* email, const char* password) {
    unsigned char salt[SALT_LENGTH];
    char encoded[ENCODED_LENGTH];

    if (generate_salt(salt, SALT_LENGTH) != 0) {
        log_error("Impossibile generare il salt");
        return false;
    }

    const int result = argon2i_hash_encoded(
        TIME_COST, MEMORY_COST, PARALLELISM,
        password, strlen(password),
        salt, SALT_LENGTH,
        HASH_LENGTH, encoded, ENCODED_LENGTH
    );

    if (result != ARGON2_OK) {
        log_error("Hashing della password fallito: %s", argon2_error_message(result));
        return false;
    }

    char query[256];
    snprintf(query, sizeof(query),
             "INSERT INTO Utente(nome, cognome, email, password) VALUES($1, $2, $3, $4) RETURNING *");

    const char* params[4] = {nome, cognome, email, encoded};
    PGresult* db_result = db_execute_query(query, 4, params);

    if (db_result == NULL) {
        log_error("Errore nell'esecuzione della query di registrazione");
        return false;
    }

    const bool success = (PQntuples(db_result) > 0);
    db_free_result(db_result);
    return success;
}