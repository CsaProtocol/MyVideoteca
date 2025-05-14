# MyVideoteca

MyVideoteca è un software per la gestione di una videoteca. Il software permette ad utenti di registrarsi al servizio, inserendo nome, cognome, email e password. Successivamente l’utente può effettuare l’accesso e effettuare le seguenti operazioni:
- cercare film per nome, regista, genere, anno, durata massima e minima, ed applicare il filtro di ordine per popolarità (è possibile applicare più filtri contemporaneamente);
- aggiungere film al carrello (ed eventualmente rimuoverli);
- noleggiare i film presenti nel carrello tramite il checkout;
- restituire i film che ha precedentemente noleggiato;
- effettuare il logout.

## Come avviare MyVideoteca
Per lo sviluppo del progetto è stato richiesto l'utilizzo di Docker Compose.

Per creare il container per il backend posizionarsi nella cartella che contiene il progetto (MyVideoteca) e eseguire il comando
` docker-compose up --build `
e per avviarlo eseguire
` docker-compose up `
