package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import it.unina.myvideoteca.MainActivity
import it.unina.myvideoteca.R
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import org.json.JSONObject

class NoleggiCaricaActivity: AppCompatActivity() {
    private lateinit var serverController : ServerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.noleggi_carica)

        val progress = findViewById<ProgressBar>(R.id.progressBar)
        progress.isVisible = true

        serverController = ServerController(SocketSingleton.client, this)

        recuperaNoleggi()

        val homeButton = findViewById<ImageView>(R.id.imgHome)
        val homeText = findViewById<TextView>(R.id.textHome)
        homeButton.setOnClickListener{ home() }
        homeText.setOnClickListener{ home() }
    }

    private fun home(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }


    private fun recuperaNoleggi(){
        serverController.recuperaNoleggi(){ response ->
            runOnUiThread {
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val risultati = jsonResponse.getString("risultati")
                        val intent = Intent(this@NoleggiCaricaActivity, NoleggiActivity::class.java)
                        intent.putExtra("risultati", risultati)
                        startActivity(intent)   //Va ai noleggi se sono stati caricati correttamente
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@NoleggiCaricaActivity, HomeActivity::class.java)
                        startActivity(intent)   //Torna alla home se ci sono errori nel caricamento dei noleggi
                    }
                } else {
                    Toast.makeText(this, "Connessione persa! Tentativo di riconnessione...", Toast.LENGTH_SHORT).show()
                    SocketSingleton.client.attemptReconnect()
                    if (SocketSingleton.client.isConnected()) {
                        // Se si è riusciti a riconnettersi al server, torna alla MainActivity
                        val intent = Intent(this@NoleggiCaricaActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()  // Chiudi l'attività corrente per evitare che l'utente possa tornare indietro
                    }
                }
            }
        }
    }

}