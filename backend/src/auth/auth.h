#ifndef AUTH_H
#define AUTH_H

#define SALT_LENGTH 16
#define HASH_LENGTH 32
#define ENCODED_LENGTH 128
#define TIME_COST 2
#define MEMORY_COST (1 << 16) // 64 MB
#define PARALLELISM 4

#include <stdbool.h>


bool login(const char* email, const char* password);
int generate_salt(unsigned char* buffer, const int length);
bool signup(const char* nome, const char* cognome, const char* email, const char* password);

#endif //AUTH_H
