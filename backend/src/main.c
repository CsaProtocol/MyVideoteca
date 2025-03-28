#include "db/postgres.h"
#include "server/socket_server.h"
#include "utils/logger.h"

#include <signal.h>
#include <stdlib.h>

static void handle_signal(const int sig) {
    log_info("Received signal %d, shutting down...", sig);
    stop_server();
    db_disconnect();
    close_logger();
    exit(0);
}

int main(int argc, char* argv[]) {
    init_logger("videoteca.log", LOG_INFO);
    log_info("Starting Videoteca Server...");

    signal(SIGINT, handle_signal);
    signal(SIGTERM, handle_signal);

    //TODO - da leggere da file di configurazione
    const db_config_t db_config = {
        .host = "localhost",
        .port = 5432,
        .dbname = "videoteca",
        .user = "postgres",
        .password = "edugau01"
    };

    //TODO - da leggere da file di configurazione
    const server_config_t server_config = {
        .port = 8080,
        .backlog = 10,
        .max_connections = 100
    };

    // Connect to databases
    if (!db_connect(db_config)) {
        log_error("Failed to connect to PostgreSQL");
        return 1;
    }

    if (!initialize_server(server_config)) {
        log_error("Failed to initialize server");
        db_disconnect();
        return 1;
    }

    start_server();

    db_disconnect();
    close_logger();

    return 0;
}
