package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.unina.myvideoteca.R
import it.unina.myvideoteca.utils.DataParser

class NoleggiActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoleggiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.noleggi)

        val risultati = intent.getStringExtra("risultati") ?: ""
        Log.d("Noleggi", "noleggi: $risultati")
        val noleggiList = DataParser.parseNoleggiList(risultati)
        Log.d("Noleggi", "noleggiList: $noleggiList")

        recyclerView = findViewById(R.id.noleggiRecyclerView)
        adapter = NoleggiAdapter(noleggiList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val nessunNoleggio = findViewById<TextView>(R.id.textVuoto)
        nessunNoleggio.isVisible = false
        recyclerView.isVisible = true

        if(noleggiList.isEmpty()){
            nessunNoleggio.isVisible = true
            recyclerView.isVisible = false
        }

        val homeButton = findViewById<ImageView>(R.id.imgHome)
        val homeText = findViewById<TextView>(R.id.textHome)
        homeButton.setOnClickListener{ home() }
        homeText.setOnClickListener{ home() }

    }

    private fun home(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

}