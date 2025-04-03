package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import it.unina.myvideoteca.R
import it.unina.myvideoteca.utils.DataParser

class CercaRisultatiActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CercaRisultatiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cerca_risultati)

        val risultati = intent.getStringExtra("risultati") ?: ""
        val filmList = DataParser.parseFilmList(risultati)

        recyclerView = findViewById(R.id.risultatiRecyclerView)
        adapter = CercaRisultatiAdapter(filmList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val risultatiText = findViewById<TextView>(R.id.textRisultati)
        risultatiText.text = getString(R.string.num_risultati, filmList.size)
        if (filmList.size < 1) recyclerView.isVisible = false

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