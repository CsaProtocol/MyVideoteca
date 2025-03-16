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