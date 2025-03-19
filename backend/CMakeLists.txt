cmake_minimum_required(VERSION 3.30)
project(backend_final C)

set(CMAKE_C_STANDARD 23)

add_executable(backend_final main.c
        database/database.h
        database/database.c
        database/postgreSQL/postgres_driver.c
        database/mariaDB/mariaDB_driver.c
)

include_directories(/usr/include/postgresql)