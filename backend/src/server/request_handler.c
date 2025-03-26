#include "request_handler.h"

#include <string.h>

#include "services/login_service.h"
#include "services/search_service.h"
#include "services/signup_service.h"
#include "utils/json.h"

char* process_request(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0 , &error);

    const char* endpoint = json_string_value(json_object_get(deSerialized, "endpoint"));
    char* response = NULL;

    if(strcmp(endpoint, "login") == 0) {
        response = login_service(request);
        return response;
    }
    if(strcmp(endpoint, "signup") == 0) {
        response = signup_service(request);
        return response;
    }
    if(strcmp(endpoint, "ricerca") == 0) {
        response = search_service(request);
        return response;
    }
    if(strcmp(endpoint, "heartbeat") == 0) {
        response = json_response_success("Heartbeat successful");
        return response;
    }
    //IF 1 endpoint noleggio
    //IF 2 endpoint restituzione
    return NULL;
}
