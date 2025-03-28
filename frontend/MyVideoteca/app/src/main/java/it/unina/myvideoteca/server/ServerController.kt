package it.unina.myvideoteca.server

import android.content.Context
import it.unina.myvideoteca.data.Film
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

    fun ricerca(titolo: String, regista: String, genere: String, anno: String,
                durataMin: String, durataMax: String, popolari: String, callback: (String?) -> Unit) {
        val jsonRequest = JSONObject().apply{
            put("endpoint", "ricerca")
            put("titolo", titolo)
            put("regista", regista)
            put("genere", genere)
            put("anno", anno)
            put("durata_min", durataMin)
            put("durata_max", durataMax)
            put("popolari", popolari)
            put("jwt_token", SharedPrefManager.getToken(context))
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
            put("jwt_token", SharedPrefManager.getToken(context))
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
            put("jwt_token", SharedPrefManager.getToken(context))
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }
}
