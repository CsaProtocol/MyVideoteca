#include <cstdio>
#include <gtest/gtest.h>

TEST(NetworkTest, CaptureResponse) {
    FILE* pipe = popen("echo -n '{\"endpoint\":\"get_noleggi\",\"userid\":\"42\"}' | nc localhost 8080", "r");
    ASSERT_TRUE(pipe) << "Failed to open pipe!";

    // Read and validate the response (example)
    char buffer[128];
    std::string response;
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        response += buffer;
    }

    int status = pclose(pipe);
    ASSERT_NE(status, -1) << "Command failed!";

    // Validate the server's response
    EXPECT_TRUE(response.find("expected_response") != std::string::npos);
}