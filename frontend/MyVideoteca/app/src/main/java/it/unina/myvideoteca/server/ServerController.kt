package it.unina.myvideoteca.server

import android.content.Context
import it.unina.myvideoteca.data.Film
import it.unina.myvideoteca.data.RicercaFilm
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.socket.SocketClient
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

    fun noleggio(carrelloList: MutableList<Film>, callback: (String?) -> Unit) {
        val jsonRequest = JSONObject().apply{
            put("endpoint", "noleggio")
            put("films", carrelloList)
            put("user_id", SharedPrefManager.getUserId(context))
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
            put("user_id", SharedPrefManager.getUserId(context))
            //put("jwt_token", SharedPrefManager.getToken(context))
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }
}
