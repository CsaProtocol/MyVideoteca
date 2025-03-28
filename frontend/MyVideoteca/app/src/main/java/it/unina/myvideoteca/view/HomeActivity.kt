package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.SharedPrefManager

class HomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val cercaButton = findViewById<Button>(R.id.buttonCerca)
        val carrelloButton = findViewById<Button>(R.id.buttonCarrello)
        val restituisciButton = findViewById<Button>(R.id.buttonRestituisci)

        if(SharedPrefManager.getNonRestituitiBool(this) == true){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("ATTENZIONE!")
            builder.setMessage("Hai dei film da restituire oltre la scadenza. Per favore restituisci i tuoi noleggi.")
            builder.setPositiveButton("OK", null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                val titleTextView = dialog.findViewById<TextView>(android.R.id.title)
                titleTextView?.setTextColor(ContextCompat.getColor(this, R.color.mv_red)) // Cambia il colore del titolo in rosso
            }
            dialog.show()
        }

        cercaButton.setOnClickListener{
            val intent = Intent(this, CercaActivity::class.java)
            startActivity(intent)
        }

        carrelloButton.setOnClickListener{
            val intent = Intent(this, CarrelloActivity::class.java)
            startActivity(intent)
        }

        restituisciButton.setOnClickListener{
            val intent = Intent(this, NoleggiCaricaActivity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /*Non fare niente (impossibile andare indietro alla pagina di login)*/ }
        })
    }
}