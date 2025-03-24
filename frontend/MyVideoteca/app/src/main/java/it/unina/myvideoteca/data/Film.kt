package it.unina.myvideoteca.data


data class Film (
    val filmId: Int,
    val titolo: String,
    val regista: String,
    val genere: String,
    val anno: Int,
    val durata: Int,
    val descrizione: String,
    val copie: Int,
    val copieDisponibili: Int
)