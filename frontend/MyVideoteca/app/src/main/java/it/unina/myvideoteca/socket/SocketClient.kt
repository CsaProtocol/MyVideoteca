package it.unina.myvideoteca.socket

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val serverIp: String, private val serverPort: Int) {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private var keepAliveThread: Thread? = null //Thread per la gestione del timeout del server
    private var running = false

    fun connect() {
        try {
            socket = Socket(serverIp, serverPort)
            writer = PrintWriter(OutputStreamWriter(socket?.getOutputStream()), true)
            reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
            running = true
            Log.d("Connessione","Connesso al server $serverIp:$serverPort")
            startKeepAlive()  // Avvio del thread Keep-Alive dopo la connessione
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Errore Connessione","Errore di connessione! Tentativo di riconnessione...")
            attemptReconnect()  // Riconnessione automatica
        }
    }

    fun isConnected(): Boolean {
        return socket?.isConnected == true
    }

    private fun startKeepAlive() {
        keepAliveThread = Thread {
            while (running) {
                try {
                    // Invia un messaggio di Keep-Alive ogni 250 secondi (prima del timeout di 300s)
                    writer?.println("{\"type\": \"heartbeat\"}")
                    Log.d("KeepAlive","Keep-Alive inviato")
                    Thread.sleep(250_000)  // Attendi 250 secondi tra un messaggio e l'altro
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("Errore KeepAlive","Errore durante il Keep-Alive: chiusura la connessione.")
                    disconnect()
                    attemptReconnect()  // Tentativo di riconnessione se si verifica un errore
                    break
                }
            }
        }
        keepAliveThread?.start()
    }

    fun sendMessage(message: String) {
        try {
            writer?.println(message)
            Log.d("sendMEssage","Messaggio inviato al server: $message")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Errore sendMessage","Errore durante l'invio del messaggio! Tentativo di riconnessione...")
            attemptReconnect()  // Tentativo di riconnessione in caso di errore
        }
    }

    fun readResponse(): String? {
        return try {
            val response = reader?.readLine()
            if (response == null) {
                Log.d("readResponse","La connessione al server è stata interrotta.")
                attemptReconnect()  // Se la risposta è null, la connessione è caduta
            }
            response
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Errore readResponse","Errore durante la lettura della risposta!")
            attemptReconnect()
            null
        }
    }

    fun attemptReconnect() {
        disconnect()  // Chiude eventuali connessioni aperte
        Log.d("attemptReconnect","Tentativo di riconnessione al server in 5 secondi...")
        Thread.sleep(5000)  // Attende 5 secondi prima di riprovare
        connect()  // Riprova a connettere
    }

    fun disconnect() {
        try {
            running = false  // Termina il thread Keep-Alive
            socket?.close()
            Log.d("disconnect","Connessione chiusa.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("errore disconnect","Errore nella chiusura della connessione.")
        }
    }
}
