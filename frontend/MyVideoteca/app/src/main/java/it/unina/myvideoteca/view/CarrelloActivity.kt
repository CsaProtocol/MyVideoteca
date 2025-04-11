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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.unina.myvideoteca.MainActivity
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import it.unina.myvideoteca.utils.DataParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            val builder = AlertDialog.Builder(this)
            builder.setTitle("ATTENZIONE!")
            val errorMessage = "Uno o più film non sono stati noleggiati. I possibili motivi sono:"
            val error1 = " • Hai già noleggiato questo film e ancora non lo hai restituito;"
            val error2 = " • Hai superato il numero di noleggi massimi per utente;"
            val error3 = " • Il film non è disponibile al momento."
            builder.setMessage("$errorMessage \n $error1 \n $error2 \n $error3 ")
            builder.setPositiveButton("OK", null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                val titleTextView = dialog.findViewById<TextView>(android.R.id.title)
                titleTextView?.setTextColor(ContextCompat.getColor(this, R.color.mv_red)) // Cambia il colore del titolo in rosso
            }
            dialog.show()
        }
    }

    private fun gestisciNoleggioSuccesso(successfulRentals: JSONArray?) {
        if (successfulRentals != null) {
            for (i in 0 until successfulRentals.length()) {
                val rental = successfulRentals.getJSONObject(i)
                val filmId = rental.optInt("film_id")
                rimuoviDalCarrello(filmId)// Rimuove i film noleggiati dal carrello
                lifecycleScope.launch {
                    delay(2500) // 3 secondi
                    recreate() // Aggiorna l'activity dopo 3 secondi
                }
            }
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

    private fun rimuoviDalCarrello(filmid: Int) {
        val userId = SharedPrefManager.getUserId(this)

        if (userId != null) {
            val userCart = SharedPrefManager.getUserCart(userId, this)
            val filmsArray = userCart.optJSONArray("films") ?: JSONArray()
            val newArray = JSONArray()

            for (i in 0 until filmsArray.length()) { // Ricrea l'array senza il film da rimuovere
                val currentFilm = filmsArray.getJSONObject(i)
                val currentId = currentFilm.optInt("film_id")
                if (currentId != filmid) {
                    newArray.put(currentFilm)
                } else {
                    Log.d("Carrello", "Film con id $filmid rimosso dal carrello")
                }
            }
            userCart.put("films", newArray)
            SharedPrefManager.saveUserCart(userId, userCart.toString(), this)
        }
    }

}