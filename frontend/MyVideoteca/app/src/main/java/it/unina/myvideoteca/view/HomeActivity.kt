package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import it.unina.myvideoteca.R

class HomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val cercaButton = findViewById<Button>(R.id.buttonCerca)
        val carrelloButton = findViewById<Button>(R.id.buttonCarrello)
        val restituisciButton = findViewById<Button>(R.id.buttonRestituisci)

        cercaButton.setOnClickListener{
            val intent = Intent(this, CercaActivity::class.java)
            startActivity(intent)
        }

        carrelloButton.setOnClickListener{
            val intent = Intent(this, CarrelloActivity::class.java)
            startActivity(intent)
        }

        restituisciButton.setOnClickListener{
            val intent = Intent(this, NoleggiActivity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /*Non fare niente (impossibile andare indietro alla pagina di login)*/ }
        })
    }
}