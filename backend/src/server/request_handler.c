#include "request_handler.h"

#include <string.h>

#include "services/all_rentals_service.h"
#include "services/login_service.h"
#include "services/rental_service.h"
#include "services/return_service.h"
#include "services/search_service.h"
#include "services/signup_service.h"
#include "utils/json.h"

char* process_request(const char* request) {
    json_error_t error;
    json_t* deSerialized = json_loadb(request, strlen(request), 0 , &error);

    if(deSerialized == NULL) {
        return json_response_error("Formato JSON non valido");
    }

    const char* endpoint = json_string_value(json_object_get(deSerialized, "endpoint"));
    char* response = NULL;
    if(!endpoint) {
        json_decref(deSerialized);
        return json_response_error("Endpoint non trovato");
    }

    if(strcmp(endpoint, "login") == 0) {
        response = login_service(request);
        json_decref(deSerialized);
        return response;
    }
    if(strcmp(endpoint, "signup") == 0) {
        response = signup_service(request);
        json_decref(deSerialized);
        return response;
    }

    //TODO - CONTROLLA JWT TOKEN

    if(strcmp(endpoint, "ricerca") == 0) {
        response = search_service(request);
        json_decref(deSerialized);
        return response;
    }
    if(strcmp(endpoint, "heartbeat") == 0) {
        response = json_response_success("Heartbeat riuscito");
        json_decref(deSerialized);
        return response;
    }
    if(strcmp(endpoint, "noleggio") == 0) {
        response = rental_service(request);
        json_decref(deSerialized);
        return response;
    }
    if(strcmp(endpoint, "restituzione") == 0) {
        response = return_service(request);
        json_decref(deSerialized);
        return response;
    }
    if(strcmp(endpoint, "get_noleggi") == 0) {
        response = all_rentals_service(request);
        json_decref(deSerialized);
        return response;
    }

    json_decref(deSerialized);
    return json_response_error("Endpoint non trovato");
}