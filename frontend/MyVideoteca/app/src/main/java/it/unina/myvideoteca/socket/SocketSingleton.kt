package it.unina.myvideoteca.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SocketSingleton {
    val client: SocketClient = SocketClient("192.168.1.100", 8080)

    init {
        // Connetti il socket su un thread separato usando coroutines
        CoroutineScope(Dispatchers.IO).launch {
            client.connect() // Connetti subito il socket quando l'app si avvia
        }
    }
}
