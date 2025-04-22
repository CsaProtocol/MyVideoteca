#include <cstdio>
#include <gtest/gtest.h>

TEST(SignupTests, EmailMissing) {
    FILE* pipe = popen(R"(echo '{"endpoint":"signup","password":"password123","nome":"Mario","cognome":"Rossi"}' | nc -w 1 localhost 8080)", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    std::string buffer;
    std::string response;
    while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe) != nullptr) {
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

    std::string buffer;
    std::string response;
    while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe) != nullptr) {
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

    std::string buffer;
    std::string response;
    while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe) != nullptr) {
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

    std::string buffer;
    std::string response;
    while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe) != nullptr) {
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

    std::string buffer;
    std::string response;
    while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_FALSE(response.find("Campi obbligatori mancanti") != std::string::npos);
}
