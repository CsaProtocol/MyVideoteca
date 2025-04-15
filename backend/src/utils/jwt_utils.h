#ifndef JWT_UTILS_H
#define JWT_UTILS_H

#include <jwt.h>
#include <stdio.h>

void generate_jwt(char *token_buffer, size_t buffer_size, const char* user_id);
int verify_jwt(const char *token);

#endif // JWT_UTILS_H