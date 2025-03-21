package it.unina.myvideoteca.socket

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val serverIp: String, private val serverPort: Int) {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    fun connect() {
        try {
            socket = Socket(serverIp, serverPort)
            writer = PrintWriter(OutputStreamWriter(socket?.getOutputStream()), true)
            reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(message: String) {
        writer?.println(message)
    }

    fun readResponse(): String? {
        return try {
            reader?.readLine()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
