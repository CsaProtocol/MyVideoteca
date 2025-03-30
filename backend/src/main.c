#include "db/postgres.h"
#include "server/socket_server.h"
#include "utils/logger.h"

#include <signal.h>
#include <stdlib.h>

#include "utils/config.h"

static void handle_signal(const int sig) {
    log_info("Ricevuto segnale %d, arresto in corso...", sig);
    stop_server();
    db_disconnect();
    close_logger();
    exit(0);
}

int main(int argc, char* argv[]) {
    init_logger("videoteca.log", LOG_INFO);
    log_info("Avvio del Server MyVideoteca...");

    signal(SIGINT, handle_signal);
    signal(SIGTERM, handle_signal);

    db_config_t db_config;
    server_config_t server_config;

    if(!load_config("properties.json", &db_config, &server_config)) {
        log_error("Impossibile caricare la configurazione");
        return 1;
    }

    if (!db_connect(db_config)) {
        log_error("Connessione a PostgreSQL fallita");
        return 1;
    }

    if (!initialize_server(server_config)) {
        log_error("Inizializzazione del server fallita");
        db_disconnect();
        return 1;
    }

    start_server();

    db_disconnect();
    close_logger();

    return 0;
}
