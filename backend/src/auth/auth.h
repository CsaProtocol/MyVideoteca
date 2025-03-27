#ifndef AUTH_H
#define AUTH_H

#include <stdbool.h>
#include <openssl/sha.h>

bool login(const char* email, const char* password);
bool signup(const char* nome, const char* cognome, const char* email, const char* password);

#endif //AUTH_H
