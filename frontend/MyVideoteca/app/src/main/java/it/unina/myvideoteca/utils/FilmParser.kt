package it.unina.myvideoteca.utils

import it.unina.myvideoteca.data.Film
import org.json.JSONObject

object FilmParser {

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

}