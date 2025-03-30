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
