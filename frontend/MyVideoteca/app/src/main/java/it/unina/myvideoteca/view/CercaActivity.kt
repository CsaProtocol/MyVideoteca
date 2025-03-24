package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import it.unina.myvideoteca.R

class CercaActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cerca)

        val cercaEditText = findViewById<EditText>(R.id.editTextCerca)
        val tipoRicercaSpinner= findViewById<Spinner>(R.id.spinnerTipoRicerca)
        tipoRicercaSpinner.setSelection(0)
        val cercaButton = findViewById<Button>(R.id.buttonCerca)
        val genereSpinner = findViewById<Spinner>(R.id.spinnerGenere)
        genereSpinner.setSelection(0)
        val genereButton = findViewById<Button>(R.id.buttonCercaGenere)


        val homeButton = findViewById<ImageView>(R.id.imgHome)
        homeButton.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

    }
}