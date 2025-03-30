package it.unina.myvideoteca.utils

import it.unina.myvideoteca.data.Film
import it.unina.myvideoteca.data.Noleggio
import org.json.JSONObject
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DataParser {

    fun parseFilmList(jsonString: String): MutableList<Film> {
        val filmList = mutableListOf<Film>()
        val jsonRisultati = JSONObject(jsonString)
        val filmArray = jsonRisultati.optJSONArray("films") ?: return filmList
        for (i in 0 until filmArray.length()) {
            val filmJson = filmArray.getJSONObject(i)
            val film = Film(
                filmId = filmJson.optInt("filmid", -1),
                titolo = filmJson.optString("titolo", "N/D"),
                regista = filmJson.optString("regista", "N/D"),
                genere = filmJson.optString("genere", "N/D"),
                anno = filmJson.optInt("anno", 0),
                durata = filmJson.optInt("durata", 0),
                descrizione = filmJson.optString("descrizione", ""),
                copie = filmJson.optInt("copie", 0),
                copieDisponibili = filmJson.optInt("copieDisponibili", 0)
            )
            filmList.add(film)
        }
        return filmList
    }

    fun parseNoleggiList(jsonString: String): MutableList<Noleggio>{
        val noleggiList = mutableListOf<Noleggio>()
        val jsonRisultati = JSONObject(jsonString)
        val noleggiArray = jsonRisultati.optJSONArray("noleggi") ?: return noleggiList
        for (i in 0 until noleggiArray.length()) {
            val noleggioJson = noleggiArray.getJSONObject(i)
            val noleggio = Noleggio(
                noleggioId = noleggioJson.optInt("noleggioid", -1),
                utenteId = noleggioJson.optInt("utenteid", -1),
                filmId = noleggioJson.optInt("filmid", -1),
                dataNoleggio = dateFormatter(noleggioJson.getString("dataNoleggio")),
                dataScadenza = dateFormatter(noleggioJson.getString("dataScadenza")),
                restituito = noleggioJson.optBoolean("restituito", false)
            )
            noleggiList.add(noleggio)
        }
        return noleggiList
    }

    fun dateFormatter(dateStr: String): Timestamp{
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)  // Formato della stringa
        val date = sdf.parse(dateStr) // Parsing della stringa in un oggetto Date
        return Timestamp(date.time)// Conversione da Date a Timestamp
    }

}