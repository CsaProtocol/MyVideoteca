#include "socket_server.h"
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <sys/socket.h>

#include "request_handler.h"
#include "../utils/logger.h"

static int server_fd = -1;
static volatile bool running = false;
static server_config_t current_config;

void* handle_client(void* client_socket_ptr) {
    const int client_socket = *((int*)client_socket_ptr);
    free(client_socket_ptr);

    char buffer[4096] = {0};
    ssize_t bytes_read;

    const char* greeting = "\n   _____          ____   ____.__    .___            __                        \n  /     \\ ___.__. \\   \\ /   /|__| __| _/____  _____/  |_  ____   ____ _____   \n /  \\ /  <   |  |  \\   Y   / |  |/ __ |/ __ \\/  _ \\   __\\/ __ \\_/ ___\\__  \\  \n/    Y    \\___  |   \\     /  |  / /_/ \\  ___(  <_> )  | \\  ___/\\  \\___ / __ \\_\n\\____|__  / ____|    \\___/   |__\\____ |\\___  >____/|__|  \\___  >\\___  >____  /\n        \\/\\/                         \\/    \\/                \\/     \\/     \\/ \n";
    if (write(client_socket, greeting, strlen(greeting)) < 0) {
        log_error("Failed to send greeting to client");
        close(client_socket);
        return NULL;
    }

    while ((bytes_read = read(client_socket, buffer, sizeof(buffer) - 1)) > 0) {
        buffer[bytes_read] = '\0';

        char* response = process_request(buffer);
        // Aggiungi '\n' alla risposta
        const size_t response_len = strlen(response);
        char* response_with_newline = malloc(response_len + 2); // +1 per '\n', +1 per '\0'
        if (!response_with_newline) {
            log_error("Allocazione memoria fallita");
            free(response);
            return NULL;
        }
        sprintf(response_with_newline, "%s\n", response);

        const ssize_t written = write(client_socket, response_with_newline, strlen(response_with_newline));
        if(written < 0) {
            log_error("Impossibile inviare risposta al client");
            free(response);
            break;
        }

        free(response);
        free(response_with_newline);
        memset(buffer, 0, sizeof(buffer));
    }

    close(client_socket);
    log_info("Client disconnesso");
    return NULL;
}

bool initialize_server(const server_config_t config) {
    current_config = config;

    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        log_error("Creazione del socket fallita");
        return false;
    }

    int opt = 1;
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) {
        log_error("Impostazione delle opzioni del socket fallita");
        close(server_fd);
        server_fd = -1;
        return false;
    }

    struct sockaddr_in address;
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(config.port);

    if (bind(server_fd, (struct sockaddr*)&address, sizeof(address)) < 0) {
        log_error("Binding fallito");
        close(server_fd);
        server_fd = -1;
        return false;
    }

    if (listen(server_fd, config.backlog) < 0) {
        log_error("Ascolto fallito");
        close(server_fd);
        server_fd = -1;
        return false;
    }

    log_info("Server inizializzato sulla porta %d", config.port);
    return true;
}

void start_server(void) {
    if (server_fd < 0) {
        log_error("Server non inizializzato");
        return;
    }

    running = true;
    log_info("Server avviato. In attesa di connessioni...");

    struct sockaddr_in address;
    int addrlen = sizeof(address);

    while (running) {
        int* client_socket = malloc(sizeof(int));
        if (!client_socket) {
            log_error("Allocazione di memoria fallita");
            continue;
        }

        if ((*client_socket = accept(server_fd, (struct sockaddr*)&address, (socklen_t*)&addrlen)) < 0) {
            free(client_socket);
            if (!running)
                break;
            log_error("Accettazione connessione fallita");
            continue;
        }

        log_info("Nuovo client connesso");

        pthread_t thread_id;
        if (pthread_create(&thread_id, NULL, handle_client, client_socket) != 0) {
            log_error("Impossibile creare il thread");
            close(*client_socket);
            free(client_socket);
            continue;
        }
        pthread_detach(thread_id);
    }
}

void stop_server(void) {
    running = false;
    if (server_fd >= 0) {
        close(server_fd);
        server_fd = -1;
    }
    log_info("Server arrestato");
}