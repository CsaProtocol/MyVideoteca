#include <cstdio>
#include <gtest/gtest.h>

TEST(LoginTests, EmailMissing) {
    FILE* pipe = popen("echo '{\"endpoint\":\"login\",\"password\":\"password123\"}' | nc -w 1 localhost 8080", "r");
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

TEST(LoginTests, PasswordMissing) {
    FILE* pipe = popen("echo '{\"endpoint\":\"login\",\"email\":\"test123@gmail.com\"}' | nc -w 1 localhost 8080", "r");
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

TEST(LoginTests, JsonComplete) {
    FILE* pipe = popen("echo '{\"endpoint\":\"login\",\"email\":\"test123@gmail.com\",\"password\":\"password123\"}' | nc -w 1 localhost 8080", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    const int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    std::cout << response << std::endl;
    EXPECT_TRUE(response.find("Login effettuato con successo") != std::string::npos);
}
