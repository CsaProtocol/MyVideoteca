#ifndef SOCKET_SERVER_H
#define SOCKET_SERVER_H

#include <stdbool.h>

typedef struct {
    int port;
    int backlog;
    int max_connections;
} server_config_t;

bool initialize_server(server_config_t config);
void start_server(void);
void stop_server(void);

#endif // SOCKET_SERVER_H