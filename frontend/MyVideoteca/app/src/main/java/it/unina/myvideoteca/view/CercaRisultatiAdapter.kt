package it.unina.myvideoteca.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.Film
import it.unina.myvideoteca.data.SharedPrefManager
import org.json.JSONObject

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
            val userId = SharedPrefManager.getUserId(context)
            if (userId != null) {
                val userCart = SharedPrefManager.getUserCart(userId, context)

                if (userCart.has((film.filmId).toString())) { // Controlla se il film è già nel carrello
                    Toast.makeText(context, "Film già presente nel carrello", Toast.LENGTH_SHORT).show()
                } else {
                    // Aggiungi il film al carrello
                    val movieJson = JSONObject().apply {
                        put("id", (film.filmId).toString())
                        put("titolo", film.titolo)
                    }
                    userCart.put((film.filmId).toString(), movieJson)
                    SharedPrefManager.saveUserCart(userId, userCart.toString(), context)

                    Toast.makeText(context, "Film aggiunto al carrello", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
