#ifndef CONFIG_H
#define CONFIG_H

#include <stdbool.h>
#include "../db/postgres.h"
#include "../server/socket_server.h"

bool load_config(const char* file_path, db_config_t* db_config, server_config_t* server_config);
bool save_config(const char* file_path, const db_config_t* db_config, const server_config_t* server_config);
void free_config(db_config_t* db_config);

#endif //CONFIG_H
