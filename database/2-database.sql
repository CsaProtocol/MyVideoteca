CREATE TABLE Utente(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    film_non_restituiti BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TYPE film_genre AS ENUM (
    'Azione',
    'Avventura',
    'Commedia',
    'Drammatico',
    'Fantasy',
    'Fantascienza',
    'Giallo',
    'Horror',
    'Musical',
    'Romantico',
    'Storico',
    'Thriller',
    'Western',
    'Animazione',
    'Documentario'
);

CREATE TABLE Film(
    film_id SERIAL PRIMARY KEY,
    titolo VARCHAR(50) NOT NULL,
    regista VARCHAR(50) NOT NULL,
    genere film_genre NOT NULL,
    anno INTEGER NOT NULL,
    durata INTEGER NOT NULL, -- in minuti
    descrizione TEXT NOT NULL,
    numero_copie INTEGER NOT NULL CHECK (numero_copie > 0),
    numero_copie_disponibili INTEGER NOT NULL CHECK (numero_copie_disponibili >= 0 AND numero_copie_disponibili <= numero_copie)
);

CREATE TABLE Noleggio(
    noleggio_id SERIAL PRIMARY KEY,
    utente_id INTEGER REFERENCES Utente(id),
    film_id INTEGER REFERENCES Film(film_id),
    data_noleggio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_scadenza TIMESTAMP NOT NULL,
    restituito BOOLEAN NOT NULL DEFAULT FALSE
);
