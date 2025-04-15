#include "jwt_utils.h"
#include "utils/logger.h"
#include <string.h>
#include <stdlib.h> 
#include <time.h>

const char* SECRET_KEY = "My!V1d3otec@";

void generate_jwt(char *token_buffer, size_t buffer_size, const char* user_id) {
    jwt_t* jwt = NULL;
    jwt_new(&jwt);

    jwt_add_grant(jwt, "userid", user_id);
    time_t curr_time = time(NULL);
    jwt_add_grant_int(jwt, "exp", curr_time + 3600);
    jwt_set_alg(jwt, JWT_ALG_HS256, (unsigned char *)SECRET_KEY, strlen(SECRET_KEY));

    const char* jwt_str = jwt_encode_str(jwt);
    snprintf(token_buffer, buffer_size, "%s", jwt_str);

    log_info("Token JWT generato: %s\n", jwt_str);

    jwt_free(jwt);
}

int verify_jwt(const char *token) {
    jwt_t* jwt = NULL;

    if (jwt_decode(&jwt, token, (unsigned char *)SECRET_KEY, strlen(SECRET_KEY)) != 0) {
        log_error("Token non valido.\n");
        return 0;
    }

    const char* exp_str = jwt_get_grant(jwt, "exp");
    if (exp_str != NULL) {
        time_t exp_time = atoi(exp_str);

        if (time(NULL) > exp_time) {
            log_error("Token scaduto.\n");
            jwt_free(jwt);
            return 0;
        }
    }

    const char* userid = jwt_get_grant(jwt, "userid");
    log_info("Token valido per l'utente con ID: %s .\n", userid);

    jwt_free(jwt);
    return 1;
}

