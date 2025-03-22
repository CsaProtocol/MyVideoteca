package it.unina.myvideoteca.server

import it.unina.myvideoteca.socket.SocketClient
import org.json.JSONObject

class ServerController(private val client: SocketClient) {

    fun signUp(nome: String, cognome: String, email: String, password: String): String? {
        // Creazione del JSON da inviare al server
        val jsonRequest = JSONObject()
        jsonRequest.put("endpoint", "signUp")
        jsonRequest.put("nome", nome)
        jsonRequest.put("cognome", cognome)
        jsonRequest.put("email", email)
        jsonRequest.put("password", password)

        // invio del messaggio e lettura della risposta
        client.sendMessage(jsonRequest.toString())
        val response = client.readResponse()

        return response
    }

    fun logIn(email: String, password: String): String? {
        // Creazione del JSON da inviare al server
        val jsonRequest = JSONObject()
        jsonRequest.put("endpoint", "logIn")
        jsonRequest.put("email", email)
        jsonRequest.put("password", password)

        // invio del messaggio e lettura della risposta
        client.sendMessage(jsonRequest.toString())
        val response = client.readResponse()

        return response
    }
}
