package it.unina.myvideoteca.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.Film

class CercaRisultatiAdapter(private val filmList: MutableList<Film>,
                            private val context: Context):
    RecyclerView.Adapter<CercaRisultatiAdapter.RisultatiViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RisultatiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cerca_risultati_card, parent, false)
        return RisultatiViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: RisultatiViewHolder, position: Int) {
        val film = filmList[position]
        holder.bind(film)
    }

    override fun getItemCount() = filmList.size

    class RisultatiViewHolder(
        view: View,
        private val context: Context
    ) : RecyclerView.ViewHolder(view) {
        val titoloText = view.findViewById<TextView>(R.id.textTitolo)
        val registaText = view.findViewById<TextView>(R.id.textRegista)
        val annoEDurataText = view.findViewById<TextView>(R.id.textAnno)
        val genereText = view.findViewById<TextView>(R.id.textGenere)
        val descrizioneText = view.findViewById<TextView>(R.id.textDescrizione)
        val copieText = view.findViewById<TextView>(R.id.textCopie)
        val aggiungiButton = view.findViewById<Button>(R.id.buttonAggiungi)

        fun bind(film : Film){
            titoloText.text = film.titolo
            registaText.text = film.regista
            annoEDurataText.text = context.getString(R.string.anno_e_durata_mockup, film.anno, film.durata)
            genereText.text = film.genere
            descrizioneText.text = film.descrizione
            copieText.text = context.getString(R.string.copie_disponibili, film.copieDisponibili)

            aggiungiButton.setOnClickListener {
                    aggiungiAlCarrello(film)
            }
        }

        private fun aggiungiAlCarrello(film: Film){
            // TODO: Quando si clicca il pulsante, chiama la funzione per aggiungere il film al carrello
        }
    }
}
