CREATE TABLE ValoriGlobali (
    nome  VARCHAR(50) PRIMARY KEY,
    valore INT NOT NULL
);

INSERT INTO ValoriGlobali(nome, valore) VALUES
    ('massimo_noleggi', 5),
    ('durata_noleggio', 60);

CREATE OR REPLACE FUNCTION get_massimo_noleggi()
RETURNS INT AS $$
BEGIN
    RETURN (SELECT valore FROM ValoriGlobali WHERE nome = 'massimo_noleggi');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_durata_noleggio()
RETURNS INT AS $$
BEGIN
    RETURN (SELECT valore FROM ValoriGlobali WHERE nome = 'durata_noleggio');
END;
$$ LANGUAGE plpgsql;

CREATE TABLE Utente(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
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

CREATE OR REPLACE FUNCTION copie_disponibili_update()
RETURNS TRIGGER AS $$
BEGIN
    IF(NEW.restituito = FALSE) THEN
        UPDATE Film
        SET numero_copie_disponibili = numero_copie_disponibili - 1
        WHERE film_id = NEW.film_id;
    ELSE
        UPDATE Film
        SET numero_copie_disponibili = numero_copie_disponibili + 1
        WHERE film_id = NEW.film_id;
    END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER copie_disponibili_trigger
AFTER INSERT OR UPDATE ON Noleggio
FOR EACH ROW
EXECUTE FUNCTION copie_disponibili_update();



-- MAX K FILM PER UTENTE
CREATE OR REPLACE FUNCTION max_film()
RETURNS TRIGGER AS $$
BEGIN
    IF(SELECT COUNT(*) FROM Noleggio WHERE utente_id = NEW.utente_id AND restituito = FALSE) >= get_massimo_noleggi() THEN
        RAISE EXCEPTION 'Non puoi noleggiare più di k film contemporaneamente';
    END IF;
	
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER max_5_film_trigger
BEFORE INSERT ON Noleggio
FOR EACH ROW
EXECUTE FUNCTION max_film();



-- DURATA NOLEGGIO
CREATE OR REPLACE FUNCTION durata_noleggio()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_scadenza = NEW.data_noleggio + INTERVAL '1 DAY' * get_durata_noleggio();
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER durata_noleggio_trigger
BEFORE INSERT ON Noleggio
FOR EACH ROW
EXECUTE FUNCTION durata_noleggio();



-- 0 FILM DISPONIBILI
CREATE OR REPLACE FUNCTION film_disponibili()
RETURNS TRIGGER AS $$
BEGIN
    IF(SELECT numero_copie_disponibili FROM Film WHERE film_id = NEW.film_id) = 0 THEN
        RAISE EXCEPTION 'Non ci sono copie disponibili di questo film';
    END IF;
	
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER film_disponibili_trigger
BEFORE INSERT ON Noleggio
FOR EACH ROW
EXECUTE FUNCTION film_disponibili();



-- AGGIORNA FLAG UTENTE
CREATE OR REPLACE FUNCTION aggiorna_flag_utente()
RETURNS TRIGGER AS $$
DECLARE
    film_in_ritardo INT;
BEGIN
    SELECT COUNT(*) INTO film_in_ritardo
    FROM Noleggio 
    WHERE utente_id = NEW.utente_id AND restituito = FALSE AND data_scadenza < CURRENT_TIMESTAMP;

    IF film_in_ritardo > 0 THEN
        UPDATE Utente
        SET film_non_restituiti = TRUE
        WHERE id = NEW.utente_id;
    ELSE
        UPDATE Utente
        SET film_non_restituiti = FALSE
        WHERE id = NEW.utente_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION aggiorna_flag_utente(in_utente_id INT)
RETURNS VOID AS $$
DECLARE
    film_in_ritardo INT;
BEGIN
    -- Conta i film non restituiti in ritardo
    SELECT COUNT(*) INTO film_in_ritardo
    FROM Noleggio 
    WHERE utente_id = in_utente_id 
      AND restituito = FALSE 
      AND data_scadenza < CURRENT_TIMESTAMP;

    -- Aggiorna il flag nella tabella Utente
    UPDATE Utente
    SET film_non_restituiti = (film_in_ritardo > 0)
    WHERE id = in_utente_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION noleggio_unico_film()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM Noleggio
        WHERE utente_id = NEW.utente_id
          AND film_id = NEW.film_id
          AND restituito = FALSE
    ) THEN
        RAISE EXCEPTION 'Hai già noleggiato questo film e non lo hai ancora restituito.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER noleggio_unico_film_trigger
BEFORE INSERT ON Noleggio
FOR EACH ROW
EXECUTE FUNCTION noleggio_unico_film();

INSERT INTO film (titolo, regista, genere, anno, durata, descrizione, numero_copie, numero_copie_disponibili)
VALUES 
('Inception', 'Christopher Nolan', 'Fantascienza', 2010, 148, 'Un ladro esperto nei sogni entra nella mente delle persone.', 5, 5),
('La La Land', 'Damien Chazelle', 'Musical', 2016, 128, 'Il film racconta la storia d amore tra un musicista jazz (Ryan Gosling) e un aspirante attrice (Emma Stone).', 5, 5),
('Orgoglio e pregiudizio', 'Joe Wright', 'Storico', 2005, 127, 'Il film basato sul celebre romanzo di Jane Austen.', 5, 5),
('Ocean s 8', 'Gary Ross', 'Azione', 2018, 111, 'La sorella di Danny Ocean progetta un bel colpo per intascarsi un sacco di soldi. Ma per farlo ha bisogno di qualche complice.', 5, 5),
('Barbie', 'Greta Gerwig', 'Commedia', 2023, 114, 'Il primo adattamento cinematografico in live action dell omonima fashion doll della Mattel.', 5, 5),
('Indiana Jones 5', 'James Mangold', 'Avventura', 2023, 142, 'Il temerario archeologo Indiana Jones lotta contro il tempo per recuperare un quadrante leggendario che può cambiare il corso della storia.', 5, 5),
('Dunkirk', 'Christopher Nolan', 'Drammatico', 2017, 106, 'L esercito inglese, impegnato a liberare le truppe alleate dalla Francia occupata dai nazisti, resta intrappolato sulla spiaggia.', 5, 5),
('Le Cronache di Narnia', 'Andrew Adamson', 'Fantasy', 2005, 150, 'Quattro fratelli scoprono una porta misteriosa che li porta in un universo incantato dove una strega ha lanciato un tremendo incantesimo.', 5, 5),
('Assassinio sull Orient Express', 'Kenneth Branagh', 'Giallo', 2017, 109, 'Un lussuoso viaggio a bordo dell Orient Express si trasforma in una corsa contro il tempo quando viene commesso un omicidio.', 5, 5),
('Unfriended', 'Levan Gabriadze', 'Horror', 2015, 83, 'Tramite un programma di messaggistica, una presenza sovrannaturale si impossessa dell identità online di una ragazza scomparsa.', 5, 5),
('La ragazza del treno', 'Tate Taylor', 'Thriller', 2016, 112, 'Una donna distrutta dalla fine del proprio matrimonio comincia ad osservare ossessivamente una coppia, apparentemente perfetta, fino a quando assiste ad una scena che la lascia scioccata.', 5, 5),
('Il buono, il brutto, il cattivo', 'Sergio Leone', 'Western', 1966, 175, 'Mentre infuria la Guerra di Secessione, tre uomini dall oscuro passato si battono per impossessarsi di un tesoro nascosto in un cimitero.', 5, 5),
('Toy Story', 'John Lasseter', 'Animazione', 1995, 81, 'La vita di Woody, un cowboy giocattolo e pupazzo preferito del suo piccolo padrono, è minacciata dall arrivo di Buzz Lightyear, un nuovo robot colorato e pieno di luci.', 5, 5),
('Amy', 'Asif Kapadia', 'Documentario', 2015, 128, 'Amy è un documentario del 2015 diretto da Asif Kapadia sulla vita della cantante Amy Winehouse, morta a soli 27 anni per abuso di alcol.', 5, 5),
('Colpa delle stelle', 'Josh Boone', 'Romantico', 2014, 126, 'Due giovani si conoscono durante le riunioni in un gruppo di sostegno per malati di cancro e si innamorano l uno dell altra.', 5, 5);