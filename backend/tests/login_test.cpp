#include <jansson.h>
#include "gtest/gtest.h"

extern "C" {
    #include "../src/services/login_service.h"
    #include "../src/db/postgres.h"
}

char* create_login_json(const char* email, const char* password) {
    json_t* root = json_object();
    json_object_set_new(root, "email", json_string(email));
    json_object_set_new(root, "password", json_string(password));
    char* json_string = json_dumps(root, JSON_ENCODE_ANY);
    json_decref(root);
    return json_string;
}

const char* extract_json_string(const char* json_string, const char* key) {
    json_error_t error;
    json_t* root = json_loads(json_string, 0, &error);
    if (!root) {
        return nullptr;
    }
    const json_t* value = json_object_get(root, key);
    if (!json_is_string(value)) {
        json_decref(root);
        return nullptr;
    }
    const char* result = json_string_value(value);
    json_decref(root);
    return result;
}

int extract_json_int(const char* json_string, const char* key) {
    json_error_t error;
    json_t* root = json_loads(json_string, 0, &error);
    if (!root) {
        return -1;
    }
    const json_t* value = json_object_get(root, key);
    if (!json_is_integer(value)) {
        json_decref(root);
        return -1;
    }
    const int result = json_integer_value(value);
    json_decref(root);
    return result;
}

TEST(LoginServiceTest, ValidLogin) {
    const auto email = "test@example.com";
    const auto password = "password";
    char* login_json = create_login_json(email, password);

    char* response_json = login_service(login_json);

    ASSERT_NE(response_json, nullptr);
    const char* status = extract_json_string(response_json, "status");
    ASSERT_STREQ(status, "success");
    const char* message = extract_json_string(response_json, "message");
    ASSERT_STREQ(message, "Login effettuato con successo");
    ASSERT_NE(extract_json_int(response_json, "id"), -1);

    free(login_json);
    free(response_json);
}

TEST(LoginServiceTest, InvalidLogin) {
    const auto email = "test@example.com";
    const auto password = "wrong_password";
    char* login_json = create_login_json(email, password);

    char* response_json = login_service(login_json);

    ASSERT_NE(response_json, nullptr);
    const char* status = extract_json_string(response_json, "status");
    ASSERT_STREQ(status, "error");
    const char* message = extract_json_string(response_json, "message");
    ASSERT_STREQ(message, "Login fallito");

    free(login_json);
    free(response_json);
}

TEST(LoginServiceTest, InvalidJson) {
    const auto login_json = "{email:\"test@example.com\", password:\"password\"";

    char* response_json = login_service(login_json);

    ASSERT_NE(response_json, nullptr);
    const char* status = extract_json_string(response_json, "status");
    ASSERT_STREQ(status, "error");
    const char* message = extract_json_string(response_json, "message");
    ASSERT_STREQ(message, "JSON non valido");

    free(response_json);
}