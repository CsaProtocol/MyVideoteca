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
static bool running = false;
static server_config_t current_config;

void* handle_client(void* client_socket_ptr) {
    const int client_socket = *((int*)client_socket_ptr);
    free(client_socket_ptr);

    char buffer[4096] = {0};
    ssize_t bytes_read;

    while ((bytes_read = read(client_socket, buffer, sizeof(buffer) - 1)) > 0) {
        buffer[bytes_read] = '\0';

        char* response = process_request(buffer);

        const ssize_t written = write(client_socket, response, strlen(response));
        if(written < 0) {
            log_error("Failed to send response to client");
            free(response);
            break;
        }

        free(response);
        memset(buffer, 0, sizeof(buffer));
    }

    close(client_socket);
    log_info("Client disconnected");
    return NULL;
}

bool initialize_server(const server_config_t config) {
    current_config = config;

    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        log_error("Socket creation failed");
        return false;
    }

    int opt = 1;
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) {
        log_error("Setsockopt failed");
        return false;
    }

    struct sockaddr_in address;
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(config.port);

    if (bind(server_fd, (struct sockaddr*)&address, sizeof(address)) < 0) {
        log_error("Bind failed");
        return false;
    }

    if (listen(server_fd, config.backlog) < 0) {
        log_error("Listen failed");
        return false;
    }

    log_info("Server initialized on port %d", config.port);
    return true;
}

void start_server(void) {
    if (server_fd < 0) {
        log_error("Server not initialized");
        return;
    }

    running = true;
    log_info("Server started. Waiting for connections...");

    struct sockaddr_in address;
    int addrlen = sizeof(address);

    while (running) {
        int* client_socket = malloc(sizeof(int));
        if ((*client_socket = accept(server_fd, (struct sockaddr*)&address, (socklen_t*)&addrlen)) < 0) {
            free(client_socket);
            if (!running)
                break;
            log_error("Accept failed");
            continue;
        }

        log_info("New client connected");

        pthread_t thread_id;
        if (pthread_create(&thread_id, NULL, handle_client, client_socket) != 0) {
            log_error("Failed to create thread");
            close(*client_socket);
            free(client_socket);
            continue;
        }

        // Detach the thread - we don't need to join it later
        pthread_detach(thread_id);
    }
}

void stop_server(void) {
    running = false;
    if (server_fd >= 0) {
        close(server_fd);
        server_fd = -1;
    }
    log_info("Server stopped");
}