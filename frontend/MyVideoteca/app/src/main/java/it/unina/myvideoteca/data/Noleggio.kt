package it.unina.myvideoteca.data

data class Noleggio (
    val noleggioId: Int,
    val filmId: Int,
    val titoloFilm: String,
    val registaFilm: String,
    val dataNoleggio: String,
    val dataScadenza: String
)