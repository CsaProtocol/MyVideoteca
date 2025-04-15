cmake_minimum_required(VERSION 3.22)

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
#                                      PROJECT FILE-STRUCTURE SETTINGS                                      #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#

project(
  LANGUAGES C CXX
)

set(CMAKE_C_STANDARD 23)
set(CMAKE_C_STANDARD_REQUIRED ON)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(MAIN_BINARY demo)
set(TEST_BINARY unit_tests)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/out/bin)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/out/lib)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/out/lib)

include_directories(${CMAKE_SOURCE_DIR}/src)
include_directories(${CMAKE_SOURCE_DIR}/src/old)

file(GLOB_RECURSE MAIN_FILE ${CMAKE_SOURCE_DIR}/src/main.c)
file(GLOB_RECURSE SOURCE_FILES ${CMAKE_SOURCE_DIR}/src/*/*.c)
file(GLOB_RECURSE SOURCE_FILES ${CMAKE_SOURCE_DIR}/src/*/*/*.c)
file(GLOB_RECURSE TEST_FILES ${CMAKE_SOURCE_DIR}/tests/*/*.cpp)

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
#                                             DEPENDENCIES                                                 #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#

set(CMAKE_PREFIX_PATH ${CMAKE_SOURCE_DIR}/dependencies/build/Release/generators ${CMAKE_PREFIX_PATH})
set(CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/dependencies/build/Release/generators ${CMAKE_MODULE_PATH})

find_package(GTest REQUIRED)
find_package(PostgreSQL REQUIRED)
find_package(jansson REQUIRED)
find_package(OpenSSL REQUIRED)
find_package(argon2 REQUIRED)
# Aggiungi i percorsi per la libreria libjwt
include_directories(/usr/include)  # Aggiungi i file header di libjwt
link_directories(/usr/lib/x86_64-linux-gnu)  # Aggiungi la directory contenente libjwt.so

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
#                                         MAIN COMPILATION OPTIONS                                         #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#

add_executable(
    ${MAIN_BINARY}
    ${MAIN_FILE}
    ${SOURCE_FILES}
        src/main.c
        src/models/film.c
        src/models/film.h
        src/db/postgres.c
        src/db/postgres.h
        src/utils/logger.c
        src/utils/logger.h
        src/server/socket_server.c
        src/server/socket_server.h
        src/server/request_handler.c
        src/server/request_handler.h
        src/utils/json.c
        src/utils/json.h
        src/auth/auth.h
        src/auth/auth.c
        src/services/login_service.c
        src/services/login_service.h
        src/services/signup_service.c
        src/services/signup_service.h
        src/services/search_service.c
        src/services/search_service.h
        src/services/rental_service.c
        src/services/rental_service.h
        src/services/return_service.c
        src/services/return_service.h
        src/services/all_rentals_service.c
        src/services/all_rentals_service.h
        src/utils/config.c
        src/utils/config.h
        src/utils/jwt_utils.c
        src/utils/jwt_utils.h
)

target_compile_options(
    ${MAIN_BINARY}
    PRIVATE
    -O3
    -Wall
    -Wpedantic
    -Wfloat-equal
    -Wno-deprecated-declarations
    -DDEBUG_BUILD
)


target_link_libraries(
    ${MAIN_BINARY}
    PRIVATE
    jansson::jansson
    PostgreSQL::PostgreSQL
    openssl::openssl
    argon2::argon2
    jwt 
)

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
#                                      "unit_tests" COMPILATION OPTIONS                                    #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#

add_executable(
    ${TEST_BINARY}
    ${SOURCE_FILES}
    ${TEST_FILES}
        src/main.c
        src/models/film.c
        src/models/film.h
        src/db/postgres.c
        src/db/postgres.h
        src/utils/logger.c
        src/utils/logger.h
        src/server/socket_server.c
        src/server/socket_server.h
        src/server/request_handler.c
        src/server/request_handler.h
        src/utils/json.c
        src/utils/json.h
        src/auth/auth.h
        src/auth/auth.c
        src/services/login_service.c
        src/services/login_service.h
        src/services/signup_service.c
        src/services/signup_service.h
        src/services/search_service.c
        src/services/search_service.h
        src/services/rental_service.c
        src/services/rental_service.h
        src/services/return_service.c
        src/services/return_service.h
        src/services/all_rentals_service.c
        src/services/all_rentals_service.h
        src/utils/config.c
        src/utils/config.h
        src/utils/jwt_utils.c
        src/utils/jwt_utils.h
        tests/all_rentals_test.cpp
        tests/all_rentals_test.h
)

target_compile_options(
    ${TEST_BINARY}
    PRIVATE
    -O0
    -Wunused
    -Wall
    -Wpedantic
    -Wswitch
    -Wfloat-equal
    -DDEBUG_BUILD
)

target_link_libraries(
    ${TEST_BINARY}
    PRIVATE
    gtest::gtest
    jansson::jansson
    PostgreSQL::PostgreSQL
    openssl::openssl
    argon2::argon2
    jwt
)

