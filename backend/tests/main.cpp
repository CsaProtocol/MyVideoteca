#include <gtest/gtest.h>

#include "setup/environment.h"

int main(int argc, char **argv) {
    testing::InitGoogleTest(&argc, argv);
    testing::AddGlobalTestEnvironment(new TestEnvironment());

    return RUN_ALL_TESTS();
}
