package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.unina.myvideoteca.MainActivity
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.Film
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import it.unina.myvideoteca.utils.DataParser
import org.json.JSONObject

class CarrelloActivity: AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CarrelloAdapter
    private lateinit var serverController: ServerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.carrello)

        val carrello = SharedPrefManager.getUserCart(SharedPrefManager.getUserId(this)?:"", this).toString()
        val carrelloList = DataParser.parseFilmList(carrello)

        recyclerView = findViewById(R.id.carrelloRecyclerView)
        adapter = CarrelloAdapter(carrelloList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        serverController = ServerController(SocketSingleton.client, this)

        val noleggiaButton = findViewById<Button>(R.id.buttonNoleggia)

        val vuotoText = findViewById<TextView>(R.id.textVuoto)
        vuotoText.isVisible = false
        if (carrelloList.isEmpty()) {
            vuotoText.isVisible = true
            recyclerView.isVisible = false
            noleggiaButton.isEnabled = false
            noleggiaButton.backgroundTintList = getColorStateList(android.R.color.darker_gray)
        }

        noleggiaButton.setOnClickListener{
            val numNonRestituiti = SharedPrefManager.getNumNonRestituiti(this) ?: 0
            val maxNoleggi = SharedPrefManager.getMaxNoleggi(this) ?: 0
            val max = maxNoleggi - numNonRestituiti

            if(carrelloList.size > max){
                showPopup("Hai troppi noleggi in corso! Rimuovi qualche film dal carrello.")
            }else if(carrelloList.isEmpty()){
                showPopup("Aggiungi dei film al carrello per proseguire con il noleggio.")
            }else{
                noleggia(carrelloList)
            }
        }

    }

    private fun showPopup(message: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ATTENZIONE!")
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val titleTextView = dialog.findViewById<TextView>(android.R.id.title)
            titleTextView?.setTextColor(ContextCompat.getColor(this, R.color.mv_red)) // Cambia il colore del titolo in rosso
        }
        dialog.show()
    }

    private fun noleggia(carrelloList: MutableList<Film>){
        serverController.noleggio(carrelloList){ response ->
            runOnUiThread {
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    /*TODO: gestisci rimuovendo elementi noleggiati con successo dal carrello*/
                } else {
                    Toast.makeText(this, "Connessione persa! Tentativo di riconnessione...", Toast.LENGTH_SHORT).show()
                    SocketSingleton.client.attemptReconnect()
                    if (SocketSingleton.client.isConnected()) {
                        // Se si è riusciti a riconnettersi al server, torna alla MainActivity
                        val intent = Intent(this@CarrelloActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()  // Chiudi l'attività corrente per evitare che l'utente possa tornare indietro
                    }
                }
            }
        }
    }

}