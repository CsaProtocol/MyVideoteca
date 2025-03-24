package it.unina.myvideoteca.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.Film

class CercaRisultatiAdapter(private val filmList: MutableList<Film>,
                            private val onRemoveClick: (String) -> Unit) :
    RecyclerView.Adapter<CercaRisultatiAdapter.RisultatiViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RisultatiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cerca_risultati_card, parent, false)
        return RisultatiViewHolder(view)
    }

    override fun onBindViewHolder(holder: RisultatiViewHolder, position: Int) {
        val film = filmList[position]
        holder.titoloText.text = film.titolo
        holder.aggiungiButton.setOnClickListener {
            // TODO: Quando si clicca il pulsante, chiama la funzione per aggiungere il film al carrello
        }
    }

    override fun getItemCount() = filmList.size

    class RisultatiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titoloText = view.findViewById<TextView>(R.id.textTitolo)
        val aggiungiButton = view.findViewById<Button>(R.id.buttonAggiungi)
    }
}
