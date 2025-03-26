package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.unina.myvideoteca.MainActivity
import it.unina.myvideoteca.R
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import org.json.JSONObject

class CercaActivity: AppCompatActivity() {
    private lateinit var serverController: ServerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cerca)

        val titoloEditText = findViewById<EditText>(R.id.editTextTitolo)
        val registaEditText = findViewById<EditText>(R.id.editTextRegista)
        val genereSpinner = findViewById<Spinner>(R.id.spinnerGenere)
        genereSpinner.setSelection(0)
        val cercaButton = findViewById<Button>(R.id.buttonCerca)

        serverController = ServerController(SocketSingleton.client)

        val homeButton = findViewById<ImageView>(R.id.imgHome)
        homeButton.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        cercaButton.setOnClickListener{
            val titolo = titoloEditText.text.toString().trim()
            val regista = registaEditText.text.toString().trim()
            val genere = genereSpinner.selectedItem.toString()

            //non faccio controlli sui campi vuoti, se tutti sono vuoti vuol dire che cerco tutti i film
            ricerca(titolo, regista, genere)
        }
    }

    private fun ricerca(titolo: String, regista: String, genere: String){
        serverController.ricerca(titolo, regista, genere){ response ->
            runOnUiThread {
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val risultati = jsonResponse.getString("risultati")
                        val intent = Intent(this@CercaActivity, CercaRisultatiActivity::class.java)
                        intent.putExtra("risultati", risultati)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Connessione persa! Tentativo di riconnessione...", Toast.LENGTH_SHORT).show()
                    SocketSingleton.client.attemptReconnect()
                    if (SocketSingleton.client.isConnected()) {
                        // Se si è riusciti a riconnettersi al server, torna alla MainActivity
                        val intent = Intent(this@CercaActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()  // Chiudi l'attività corrente per evitare che l'utente possa tornare indietro
                    }
                }
            }
        }
    }
}