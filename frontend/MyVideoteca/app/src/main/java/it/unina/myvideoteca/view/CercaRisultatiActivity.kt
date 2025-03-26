package it.unina.myvideoteca.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.Film
import org.json.JSONArray

class CercaRisultatiActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CercaRisultatiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cerca_risultati)

        val risultati = intent.getStringExtra("risultati") ?: ""
        val filmList = parseFilmList(risultati)

        recyclerView = findViewById(R.id.risultatiRecyclerView)
        adapter = CercaRisultatiAdapter(filmList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun parseFilmList(jsonString: String): MutableList<Film> {
        val filmList = mutableListOf<Film>()
        val filmArray = JSONArray(jsonString) //TODO: da correggere in base a come vengono passati realmente i film
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