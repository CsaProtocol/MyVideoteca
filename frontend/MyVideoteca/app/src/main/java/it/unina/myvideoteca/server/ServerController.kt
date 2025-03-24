package it.unina.myvideoteca.server

import it.unina.myvideoteca.socket.SocketClient
import org.json.JSONObject

class ServerController(private val client: SocketClient) {

    fun signUp(nome: String, cognome: String, email: String, password: String, callback: (String?) -> Unit) {
        val jsonRequest = JSONObject().apply {
            put("endpoint", "signUp")
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
            put("endpoint", "logIn")
            put("email", email)
            put("password", password)
        }

        client.sendMessage(jsonRequest.toString())
        client.readResponse { response ->
            callback(response)
        }
    }
}
