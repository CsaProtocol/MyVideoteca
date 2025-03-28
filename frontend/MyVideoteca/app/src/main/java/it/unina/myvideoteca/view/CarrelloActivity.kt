package it.unina.myvideoteca.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.utils.FilmParser

class CarrelloActivity: AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CarrelloAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.carrello)

        val carrello = SharedPrefManager.getUserCart(SharedPrefManager.getUserId(this)?:"", this).toString()
        val carrelloList = FilmParser.parseFilmList(carrello)

        recyclerView = findViewById(R.id.carrelloRecyclerView)
        adapter = CarrelloAdapter(carrelloList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val checkOutButton = findViewById<Button>(R.id.buttonCheckOut)

        val vuotoText = findViewById<TextView>(R.id.textVuoto)
        vuotoText.isVisible = false
        if (carrelloList.isEmpty()) {
            vuotoText.isVisible = true
            recyclerView.isVisible = false
            checkOutButton.isEnabled = false
            checkOutButton.backgroundTintList = getColorStateList(android.R.color.darker_gray)
        }

        //TODO: Gestione pulsante check-out.
    }

}