package it.unina.myvideoteca.utils

import it.unina.myvideoteca.data.Film
import it.unina.myvideoteca.data.Noleggio
import org.json.JSONObject

object DataParser {

    fun parseFilmList(jsonString: String): MutableList<Film> {
        val filmList = mutableListOf<Film>()
        val jsonRisultati = JSONObject(jsonString)
        val filmArray = jsonRisultati.optJSONArray("films") ?: return filmList
        for (i in 0 until filmArray.length()) {
            val filmJson = filmArray.getJSONObject(i)
            val film = Film(
                filmId = filmJson.optInt("id", -1),
                titolo = filmJson.optString("titolo", "N/D"),
                regista = filmJson.optString("regista", "N/D"),
                genere = filmJson.optString("genere", "N/D"),
                anno = filmJson.optInt("anno", 0),
                durata = filmJson.optInt("durata", 0),
                descrizione = filmJson.optString("descrizione", ""),
                copie = filmJson.optInt("numero_copie", 0),
                copieDisponibili = filmJson.optInt("numero_copie_disponibili", 0)
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
                noleggioId = noleggioJson.optInt("noleggio_id", -1),
                filmId = noleggioJson.optInt("filmid", -1),
                titoloFilm = noleggioJson.getString("titolo_film"),
                registaFilm = noleggioJson.getString("regista_film"),
                dataNoleggio = noleggioJson.getString("data_noleggio"),
                dataScadenza = noleggioJson.getString("data_scadenza")
            )
            noleggiList.add(noleggio)
        }
        return noleggiList
    }

}