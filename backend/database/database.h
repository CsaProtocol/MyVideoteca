#ifndef DATABASE_H
#define DATABASE_H

typedef struct DBResult {
    void* data;
    unsigned long long row_count;
    unsigned int column_count;
} DBResult;

typedef struct DBHandle {
    void* connection;
    DBResult* result;
    int error_code;
    char error_message[256];
} DBHandle;

typedef struct DBParams {
    const char* host;
    const char* port;
    const char* database_name;
    const char* username;
    const char* password;
} DBParams;

typedef struct {
    int (*connect)(DBHandle* handle, const DBParams* parameters);
    int (*execute)(DBHandle* handle, const char* query);
    DBResult* (*query)(DBHandle* handle, const char* query);
    const char* (*get_cell)(DBHandle* result, int row, int column);
    void (*free_result)(DBResult* result);
    int (*disconnect)(DBHandle* handle);
} DBDriver;

#endif
