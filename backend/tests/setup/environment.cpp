#include "environment.h"

void TestEnvironment::SetUp() {
    std::jthread environment_thread(environment_setup);
    thread_id = environment_thread.get_id();
    environment_thread.detach();
    std::this_thread::sleep_for(std::chrono::seconds(5));
}

void TestEnvironment::TearDown() {
    log_info("Esecuzione teardown dell'ambiente di test...");
    stop_server();
    db_disconnect();
    close_logger();
}

void TestEnvironment::environment_setup() {
    init_logger("videoteca_test.log", LOG_INFO);
    log_info("Avvio del Server MyVideoteca...");

    db_config_t db_config;
    server_config_t server_config;

    if(!load_config("properties.json", &db_config, &server_config)) {
        log_error("Impossibile caricare la configurazione");
    }

    if (!db_connect(db_config)) {
        log_error("Connessione a PostgreSQL fallita");
    }

    if (!initialize_server(server_config)) {
        log_error("Inizializzazione del server fallita");
        db_disconnect();
    }

    start_server();
}