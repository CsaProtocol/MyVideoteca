package it.unina.myvideoteca.socket

import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val serverIp: String, private val serverPort: Int) {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private var running = false
    private var reconnecting = false  // Evita tentativi multipli di riconnessione parallela

    private var dispatcherIO: CoroutineDispatcher = Dispatchers.IO
    private var dispatcherMain: CoroutineDispatcher = Dispatchers.Main

    // Un unico CoroutineScope per l'intera classe
    private val clientScope = CoroutineScope(dispatcherIO)

    fun connect() {
        clientScope.launch {
            try {
                Log.d("SocketClient", "Tentativo di connessione a $serverIp:$serverPort...")
                socket = Socket(serverIp, serverPort)
                Log.d("SocketClient", "Socket creato con successo!")
                writer = PrintWriter(OutputStreamWriter(socket?.getOutputStream()), true)
                reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
                running = true

                Log.d("Connessione", "Connesso al server $serverIp:$serverPort")
                startKeepAlive()
            } catch (e: Exception) {
                Log.e("Errore Connessione", "Errore di connessione! Tentativo di riconnessione...", e)
                attemptReconnect()
            }finally {
                reconnecting = false  // Resetta lo stato di riconnessione
            }
        }
    }

    fun isConnected(): Boolean {
        return socket?.isConnected == true && running
    }

    private fun startKeepAlive() {
        clientScope.launch {
            while (running) {
                try {
                    writer?.println("{\"endpoint\": \"heartbeat\"}")
                    Log.d("KeepAlive", "Keep-Alive inviato")
                    delay(250_000L)  // Attendi 250 secondi tra un messaggio e l'altro
                } catch (e: Exception) {
                    Log.e("Errore KeepAlive", "Errore durante il Keep-Alive.", e)
                    disconnect()
                    attemptReconnect()
                    break
                }
            }
        }
    }

    fun sendMessage(message: String) {
        clientScope.launch {
            try {
                writer?.println(message)
                Log.d("sendMessage", "Messaggio inviato al server: $message")
            } catch (e: Exception) {
                Log.e("Errore sendMessage", "Errore durante l'invio del messaggio!", e)
                attemptReconnect()
            }
        }
    }

    fun readResponse(callback: (String?) -> Unit) {
        clientScope.launch {
            try {
                var response: String?
                do {
                    response = reader?.readLine()
                    Log.d("readResponse", "Messaggio ricevuto: $response")

                    if (response != null) {
                        if (!response.trim().startsWith("{")) {
                            Log.d("readResponse", "Messaggio ignorato perché non è JSON: $response")
                            continue
                        }

                        val jsonResponse = JSONObject(response)
                        Log.d("readResponse", "JSON decodificato: $jsonResponse")

                        if (jsonResponse.optString("status") == "success" && jsonResponse.optString("message") == "Heartbeat successful") {
                            Log.d("readResponse", "Risposta heartbeat ricevuta e ignorata.")
                        } else {
                            withContext(dispatcherMain) {
                                Log.d("readResponse", "Invocazione callback con risposta: $response")
                                callback(response)
                            }
                            break // Esce dal ciclo una volta elaborata la prima risposta utile
                        }
                    } else {
                        Log.d("readResponse", "Risposta nulla ricevuta.")
                        break
                    }
                } while (true)
            } catch (e: Exception) {
                Log.e("Errore readResponse", "Errore durante la lettura della risposta.", e)
                withContext(dispatcherMain) {
                    Log.d("readResponse", "Invocazione callback con valore null per errore.")
                    callback(null)
                }
            }
        }
    }



    fun attemptReconnect() {
        if (reconnecting) return  // Evita più tentativi paralleli
        reconnecting = true

        clientScope.launch {
            disconnect()  // Chiudi le risorse esistenti
            Log.d("attemptReconnect", "Tentativo di riconnessione al server in 5 secondi...")
            delay(5000)
            Log.d("attemptReconnect", "Tentativo di riconnessione...")
            connect()  // Riprova la connessione
        }
    }

    fun disconnect() {
        running = false  // Termina il Keep-Alive

        try {
            writer?.close()
            reader?.close()
            socket?.close()
            Log.d("disconnect", "Connessione chiusa.")
        } catch (e: Exception) {
            Log.e("errore disconnect", "Errore nella chiusura della connessione.", e)
        } finally {
            writer = null
            reader = null
            socket = null
        }
    }
}
