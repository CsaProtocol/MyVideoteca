#include <cstdio>
#include <gtest/gtest.h>

TEST(ServiceTests, EmailMissing) {
    FILE* pipe = popen("echo -n '{\"endpoint\":\"signup\",\"password\":\"qualcosa\",\"nome\":\"giovanni\",\"cognome\":\"rossi\"}' | nc localhost 8080", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    // Validate the server's response
    EXPECT_TRUE(response.find("Campi obbligatori mancanti") != std::string::npos);
}