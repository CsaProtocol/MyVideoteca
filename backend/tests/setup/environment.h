#ifndef ENVIRONMENT_H
#define ENVIRONMENT_H

#include <gtest/gtest.h>
#include <thread>

extern "C" {
    #include "db/postgres.h"
    #include "server/socket_server.h"
    #include "utils/logger.h"

    #include <signal.h>
    #include <stdlib.h>

    #include "utils/config.h"
}

class TestEnvironment final : public testing::Environment {
public:
    void SetUp() override;
    void TearDown() override;

protected:
    static void environment_setup();

private:
    std::thread::id thread_id;

};


#endif //ENVIRONMENT_H
