package it.unina.myvideoteca.socket

object SocketSingleton {
    val client: SocketClient = SocketClient("192.168.1.100", 8080)

    init {
        client.connect()  // Connetti subito il socket quando l'app si avvia
    }
}
