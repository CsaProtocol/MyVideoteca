package it.unina.myvideoteca.server

import it.unina.myvideoteca.socket.SocketClient
import org.json.JSONObject

class ServerController(private val client: SocketClient) {

    fun registerUser(name: String, surname: String, email: String, password: String): String? {
        // Creazione del JSON da inviare al server
        val jsonRequest = JSONObject()
        jsonRequest.put("endpoint", "registrazione")
        jsonRequest.put("nome", name)
        jsonRequest.put("cognome", surname)
        jsonRequest.put("email", email)
        jsonRequest.put("password", password)

        // Connessione al server, invio del messaggio e lettura della risposta
        client.connect()
        client.sendMessage(jsonRequest.toString())
        val response = client.readResponse()
        client.disconnect()

        return response
    }
}
