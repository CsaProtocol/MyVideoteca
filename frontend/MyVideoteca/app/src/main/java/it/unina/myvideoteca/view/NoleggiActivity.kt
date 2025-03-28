package it.unina.myvideoteca.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.unina.myvideoteca.R
import it.unina.myvideoteca.utils.DataParser

class NoleggiActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.noleggi)

        val risultati = intent.getStringExtra("risultati") ?: ""
        val noleggiList = DataParser.parseNoleggiList(risultati)

    }

}