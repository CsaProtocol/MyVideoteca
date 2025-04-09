package it.unina.myvideoteca.server

import android.content.Context
import it.unina.myvideoteca.data.CarrelloFilm
import it.unina.myvideoteca.data.Film
import it.unina.myvideoteca.data.RicercaFilm
import it.unina.myvideoteca.data.Noleggio
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.socket.SocketClient
import org.json.JSONArray
import org.json.JSONObject

class ServerController(private val client: SocketClient, private val context: Context) {

    fun signUp(nome: String, cognome: String, email: String, password: String, callback: (String?) -> Unit) {
        val jsonRequest = JSONObject().apply {
            put("endpoint", "signup")
            put("nome", nome)
            put("cognome", cognome)
            put("email", email)
            put("password", password)
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }

    fun logIn(email: String, password: String, callback: (String?) -> Unit) {
        val jsonRequest = JSONObject().apply{
            put("endpoint", "login")
            put("email", email)
            put("password", password)
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }

    fun ricerca(filtri: RicercaFilm, callback: (String?) -> Unit) {
        val jsonRequest = JSONObject().apply{
            put("endpoint", "ricerca")
            put("titolo", filtri.titolo)
            put("regista", filtri.regista)
            put("genere", filtri.genere)
            put("anno", filtri.anno)
            put("durata_min", filtri.durataMin)
            put("durata_max", filtri.durataMax)
            put("popolari", filtri.popolari)
            //put("jwt_token", SharedPrefManager.getToken(context))
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }

    fun noleggio(callback: (String?) -> Unit) {
        val userId = SharedPrefManager.getUserId(context)
        val userCart = SharedPrefManager.getUserCart(userId, context)
        val filmsArray = userCart.optJSONArray("films") ?: JSONArray()

        val filmIdsArray = JSONArray()
        for (i in 0 until filmsArray.length()) {
            val film = filmsArray.getJSONObject(i)
            filmIdsArray.put(film.getInt("film_id"))
        }

        val jsonRequest = JSONObject().apply {
            put("endpoint", "noleggio")
            put("films", filmIdsArray)
            put("user_id", userId?.toIntOrNull() ?: -1)
            //put("jwt_token", SharedPrefManager.getToken(context))
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }


    fun recuperaNoleggi(callback: (String?) -> Unit){
        val jsonRequest = JSONObject().apply{
            put("endpoint", "get_noleggi")
            put("userid", SharedPrefManager.getUserId(context))
            //put("jwt_token", SharedPrefManager.getToken(context))
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }

    fun restituisciNoleggio(noleggio: Noleggio, callback: (String?) -> Unit){
        val jsonRequest = JSONObject().apply{
            put("endpoint", "restituzione")
            put("user_id", SharedPrefManager.getUserId(context))
            put("film_id", noleggio.filmId)
            put("rental_id", (noleggio.noleggioId).toString())
            //put("jwt_token", SharedPrefManager.getToken(context))
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }
}
