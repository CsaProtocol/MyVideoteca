#ifndef JSON_H
#define JSON_H

#include <jansson.h>

char* json_response_success(const char* message);
char* json_response_error(const char* message);
char* json_dumps_safe(json_t* json);

#endif // JSON_H