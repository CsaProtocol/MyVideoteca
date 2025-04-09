package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
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
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import it.unina.myvideoteca.utils.DataParser
import org.json.JSONArray
import org.json.JSONObject

class CarrelloActivity: AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CarrelloAdapter
    private lateinit var serverController: ServerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.carrello)

        val carrello = SharedPrefManager.getUserCart(SharedPrefManager.getUserId(this)?:"", this).toString()
        Log.d("Carrello", "user cart: $carrello")
        val carrelloList = DataParser.parseCarrelloList(carrello)

        recyclerView = findViewById(R.id.carrelloRecyclerView)
        adapter = CarrelloAdapter(carrelloList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.isVisible = true

        serverController = ServerController(SocketSingleton.client, this)

        val noleggiaButton = findViewById<Button>(R.id.buttonNoleggia)
        noleggiaButton.isEnabled = true
        noleggiaButton.backgroundTintList = getColorStateList(R.color.mv_purple)

        val vuotoText = findViewById<TextView>(R.id.textVuoto)
        vuotoText.isVisible = false
        if (carrelloList.isEmpty()) {
            vuotoText.isVisible = true
            recyclerView.isVisible = false
            noleggiaButton.isEnabled = false
            noleggiaButton.backgroundTintList = getColorStateList(android.R.color.darker_gray)
        }

        val homeButton = findViewById<ImageView>(R.id.imgHome)
        val homeText = findViewById<TextView>(R.id.textHome)
        homeButton.setOnClickListener{ home() }
        homeText.setOnClickListener{ home() }

        noleggiaButton.setOnClickListener{
            val numNonRestituiti = SharedPrefManager.getNumNonRestituiti(this) ?: 0
            val maxNoleggi = SharedPrefManager.getMaxNoleggi(this) ?: 0
            val max = maxNoleggi - numNonRestituiti

            if(carrelloList.size > max){
                showPopup("Hai troppi noleggi in corso! Rimuovi qualche film dal carrello o restituisci dei noleggi.")
            }else if(carrelloList.isEmpty()){
                showPopup("Aggiungi dei film al carrello per proseguire con il noleggio.")
            }else{
                noleggia()
            }
        }

    }

    private fun home(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
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

    private fun noleggia() {
        serverController.noleggio { response ->
            runOnUiThread {
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") != "success") {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                    val successfulRentals = jsonResponse.optJSONArray("successful_rentals")
                    val failedRentals = jsonResponse.optJSONArray("failed_rentals")

                    // Gestione film non noleggiati
                    gestisciNoleggioFallito(failedRentals)

                    // Gestione film noleggiati con successo
                    gestisciNoleggioSuccesso(successfulRentals)
                } else {
                    gestisciConnessionePersa()
                }
            }
        }
    }

    private fun gestisciNoleggioFallito(failedRentals: JSONArray?) {
        if (failedRentals != null && failedRentals.length() > 0) {
            val errorMessage = "Alcuni film non sono stati noleggiati. Puoi riprovare quando saranno disponibili o avrai restituito altri film."
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun gestisciNoleggioSuccesso(successfulRentals: JSONArray?) {
        if (successfulRentals != null) {
            val userId = SharedPrefManager.getUserId(this)
            val userCart = SharedPrefManager.getUserCart(userId, this)

            for (i in 0 until successfulRentals.length()) {
                val rental = successfulRentals.getJSONObject(i)
                val filmId = rental.optInt("film_id")
                userCart.remove(filmId.toString()) // Rimuove i film noleggiati dal carrello
            }

            SharedPrefManager.saveUserCart(userId, userCart.toString(), this)
            recreate() // Aggiorna l'activity
        }
    }

    private fun gestisciConnessionePersa() {
        Toast.makeText(this, "Connessione persa! Tentativo di riconnessione...", Toast.LENGTH_SHORT).show()
        SocketSingleton.client.attemptReconnect()

        if (SocketSingleton.client.isConnected()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Chiude l'activity corrente
        }
    }

}