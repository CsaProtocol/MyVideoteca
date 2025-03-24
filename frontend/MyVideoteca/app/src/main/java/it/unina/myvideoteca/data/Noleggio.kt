package it.unina.myvideoteca.data

import java.sql.Timestamp

data class Noleggio (
    val noleggioId: Int,
    val utenteId: Int,
    val filmId: Int,
    val dataNoleggio: Timestamp,
    val dataScadenza: Timestamp,
    val restituito: Boolean
)