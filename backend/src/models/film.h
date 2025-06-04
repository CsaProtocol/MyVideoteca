#ifndef FILM_H
#define FILM_H

#include <stdbool.h>
#include <time.h>

typedef struct {
    int id;
    char title[100];
    char director[100];
    char genre[50];
    int year;
    int total_copies;
    int available_copies;
    int popularity_score;
    time_t created_at;
} film_t;

film_t* film_create(const char* title, const char* director, const char* genre, 
                  int year, int total_copies);

#endif // FILM_H