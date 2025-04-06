package it.unina.myvideoteca.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.Film
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.utils.UrlFormatter
import org.json.JSONObject
import org.json.JSONArray

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
        private val filmImg = view.findViewById<ImageView>(R.id.imgFilm)
        private val titoloText = view.findViewById<TextView>(R.id.textTitolo)
        private val registaText = view.findViewById<TextView>(R.id.textRegista)
        private val annoEDurataText = view.findViewById<TextView>(R.id.textAnno)
        private val genereText = view.findViewById<TextView>(R.id.textGenere)
        private val descrizioneText = view.findViewById<TextView>(R.id.textDescrizione)
        private val copieText = view.findViewById<TextView>(R.id.textCopie)
        private val aggiungiButton = view.findViewById<Button>(R.id.buttonAggiungi)

        fun bind(film : Film){
            val imageUrl = UrlFormatter.getMoviePosterUrl(film.titolo)
            Glide.with(context)
                .load(imageUrl)
                .error(R.drawable.mv_logo_light) // Immagine di fallback in caso di errore
                .into(filmImg)
            titoloText.text = film.titolo
            registaText.text = film.regista
            annoEDurataText.text = context.getString(R.string.anno_e_durata_mockup, film.anno, film.durata)
            genereText.text = film.genere
            descrizioneText.text = film.descrizione
            copieText.text = context.getString(R.string.copie_disponibili, film.copieDisponibili)

            if (film.copieDisponibili < 1){
                aggiungiButton.isEnabled = false
                aggiungiButton.backgroundTintList = context.getColorStateList(android.R.color.darker_gray)
            }

            aggiungiButton.setOnClickListener {
                    aggiungiAlCarrello(film)
            }
        }

        private fun aggiungiAlCarrello(film: Film) {
            val userId = SharedPrefManager.getUserId(context)
            if (userId != null) {
                val userCart = SharedPrefManager.getUserCart(userId, context)
                val filmsArray = userCart.optJSONArray("films") ?: JSONArray()

                var alreadyInCart = false   // Controlla se il film è già presente nel carrello
                for (i in 0 until filmsArray.length()) {
                    val existingFilm = filmsArray.getJSONObject(i)
                    if (existingFilm.optInt("film_id") == film.filmId) {
                        alreadyInCart = true
                        break
                    }
                }

                if (alreadyInCart) {
                    Toast.makeText(context, "Film già presente nel carrello", Toast.LENGTH_SHORT).show()
                } else {    //Aggiungi film al carrello
                    val movieJson = JSONObject().apply {
                        put("film_id", film.filmId)
                        put("titolo", film.titolo)
                        put("regista", film.regista)
                    }
                    filmsArray.put(movieJson)
                    userCart.put("films", filmsArray)
                    SharedPrefManager.saveUserCart(userId, userCart.toString(), context) // Salva il carrello aggiornato
                    Log.d("aggiungiAlCarrello", "Carrello dopo l'aggiunta: $userCart")
                    Toast.makeText(context, "Film aggiunto al carrello", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}
