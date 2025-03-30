#ifndef POSTGRES_H
#define POSTGRES_H

#include <libpq-fe.h>
#include <stdbool.h>

typedef struct {
    char* host;
    int port;
    char* dbname;
    char* user;
    char* password;
} db_config_t;

bool db_connect(db_config_t config);
void db_disconnect(void);
PGresult* db_execute_query(const char* query, int param_count, const char* params[]);
void db_free_result(PGresult* result);
bool db_begin_transaction(void);
bool db_commit_transaction(void);
bool db_rollback_transaction(void);

#endif // POSTGRES_H