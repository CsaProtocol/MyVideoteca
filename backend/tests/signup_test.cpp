#include <jansson.h>
#include "gtest/gtest.h"

extern "C" {
    #include "../src/services/signup_service.h"
    #include "../src/db/postgres.h"
}

char* create_signup_json(const char* email, const char* password, const char* nome, const char* cognome) {
    json_t* root = json_object();
    json_object_set_new(root, "email", json_string(email));
    json_object_set_new(root, "password", json_string(password));
    json_object_set_new(root, "nome", json_string(nome));
    json_object_set_new(root, "cognome", json_string(cognome));
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

TEST(SignupServiceTest, ValidSignup) {
    const auto email = "new_user@example.com";
    const auto password = "secure_password";
    const auto nome = "John";
    const auto cognome = "Doe";

    char* signup_json = create_signup_json(email, password, nome, cognome);
    char* response_json = signup_service(signup_json);

    ASSERT_NE(response_json, nullptr);
    const char* status = extract_json_string(response_json, "status");
    ASSERT_STREQ(status, "success");
    const char* message = extract_json_string(response_json, "message");
    ASSERT_STREQ(message, "Registrazione completata con successo");

    free(signup_json);
    free(response_json);
}

TEST(SignupServiceTest, MissingFields) {
    json_t* root = json_object();
    json_object_set_new(root, "email", json_string("user@example.com"));

    char* signup_json = json_dumps(root, JSON_ENCODE_ANY);
    json_decref(root);

    char* response_json = signup_service(signup_json);

    ASSERT_NE(response_json, nullptr);
    const char* status = extract_json_string(response_json, "status");
    ASSERT_STREQ(status, "error");
    const char* message = extract_json_string(response_json, "message");
    ASSERT_STREQ(message, "Campi obbligatori mancanti");

    free(signup_json);
    free(response_json);
}

TEST(SignupServiceTest, InvalidJson) {
    const auto signup_json = "{email:\"test@example.com\", password:\"password\", nome:\"John\", cognome:\"Doe\"";

    char* response_json = signup_service(signup_json);

    ASSERT_NE(response_json, nullptr);
    const char* status = extract_json_string(response_json, "status");
    ASSERT_STREQ(status, "error");
    const char* message = extract_json_string(response_json, "message");
    ASSERT_STREQ(message, "JSON non valido");

    free(response_json);
}

TEST(SignupServiceTest, DuplicateUser) {
    const auto email = "duplicate@example.com";
    const auto password = "password";
    const auto nome = "Jane";
    const auto cognome = "Doe";

    char* first_signup = create_signup_json(email, password, nome, cognome);
    char* first_response = signup_service(first_signup);
    free(first_response);

    char* second_signup = create_signup_json(email, password, nome, cognome);
    char* second_response = signup_service(second_signup);

    ASSERT_NE(second_response, nullptr);
    const char* status = extract_json_string(second_response, "status");
    ASSERT_STREQ(status, "error");
    const char* message = extract_json_string(second_response, "message");
    ASSERT_STREQ(message, "Registrazione fallita");

    free(first_signup);
    free(second_signup);
    free(second_response);
}