#include <cstdio>
#include <gtest/gtest.h>

TEST(SignupTests, EmailMissing) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","password":"password123","nome":"Mario","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Campi obbligatori mancanti") != std::string::npos);
}

TEST(SignupTests, PasswordMissing) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","email":"test123@gmail.com","nome":"Mario","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Campi obbligatori mancanti") != std::string::npos);
}

TEST(SignupTests, NameMissing) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","email":"test123@gmail.com","password":"password123","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Campi obbligatori mancanti") != std::string::npos);
}

TEST(SignupTests, SurnameMissing) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","email":"test123@gmail.com","password":"password123","nome":"Giovanni"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Campi obbligatori mancanti") != std::string::npos);
}

TEST(SignupTests, JsonComplete) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","email":"test123@gmail.com","password":"password123","nome":"Mario","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_FALSE(response.find("Campi obbligatori mancanti") != std::string::npos);
}

TEST(SignupTests, InvalidEmailFormat) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","email":"invalidemailformat","password":"password123","nome":"Mario","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Email non valida") != std::string::npos);
}

TEST(SignupTests, WeakPassword) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","email":"test123@gmail.com","password":"123","nome":"Mario","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Password troppo debole") != std::string::npos);
}

TEST(SignupTests, DuplicateEmail) {
    // First registration
    FILE* pipe1 = popen(R"(echo '{"endpoint":"signup","email":"duplicate@gmail.com","password":"password123","nome":"Mario","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe1) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe1) != nullptr) {
        response += buffer;
    }

    const int status1 = pclose(pipe1);
    ASSERT_NE(status1, -1) << "Command failed!";

    // Second registration with same email
    FILE* pipe2 = popen(R"(echo '{"endpoint":"signup","email":"duplicate@gmail.com","password":"password456","nome":"Luigi","cognome":"Verdi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe2) << "Failed to open pipe!";

    response.clear();
    while (fgets(buffer, sizeof(buffer), pipe2) != nullptr) {
        response += buffer;
    }

    const int status2 = pclose(pipe2);
    ASSERT_NE(status2, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Email già registrata") != std::string::npos);
}

TEST(SignupTests, SpecialCharactersInName) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","email":"test123@gmail.com","password":"password123","nome":"Mario<script>","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Caratteri non consentiti") != std::string::npos);
}

TEST(SignupTests, EmptyFields) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","email":"","password":"","nome":"","cognome":""}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Campi obbligatori mancanti") != std::string::npos);
}

TEST(SignupTests, VeryLongFields) {
    std::string longString(256, 'a');
    std::string command = R"(echo '{"endpoint":"signup","email":"test123@gmail.com","password":"password123","nome":")" + longString + R"(","cognome":"Rossi"}' | nc -w 1 localhost 8080)";

    FILE* pipe = popen(command.c_str(), "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[512];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Campo troppo lungo") != std::string::npos);
}

