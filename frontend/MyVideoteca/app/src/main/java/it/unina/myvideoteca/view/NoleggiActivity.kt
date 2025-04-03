package it.unina.myvideoteca.view

import android.os.Bundle
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
        val noleggiList = DataParser.parseNoleggiList(risultati)

        recyclerView = findViewById(R.id.noleggiRecyclerView)
        adapter = NoleggiAdapter(noleggiList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val nessunNoleggio = findViewById<TextView>(R.id.textVuoto)
        if(noleggiList.size > 0){
            nessunNoleggio.isVisible = false
            recyclerView.isVisible = true
        }else{
            nessunNoleggio.isVisible = true
            recyclerView.isVisible = false
        }

    }

}