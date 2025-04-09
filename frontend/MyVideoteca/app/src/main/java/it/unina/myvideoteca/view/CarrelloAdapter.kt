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
import it.unina.myvideoteca.data.CarrelloFilm
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.utils.UrlFormatter
import org.json.JSONArray

class CarrelloAdapter(private val filmList: MutableList<CarrelloFilm>,
                            private val context: Context
): RecyclerView.Adapter<CarrelloAdapter.CarrelloViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrelloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.carrello_card, parent, false)
        return CarrelloViewHolder(view, context){ position ->
            filmList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onBindViewHolder(holder: CarrelloViewHolder, position: Int) {
        val film = filmList[position]
        holder.bind(film)
    }

    override fun getItemCount() = filmList.size

    class CarrelloViewHolder(
        view: View,
        private val context: Context,
        private val notifyChange: (Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val filmImg = view.findViewById<ImageView>(R.id.imgFilm)
        private val titoloText = view.findViewById<TextView>(R.id.textTitolo)
        private val registaText = view.findViewById<TextView>(R.id.textRegista)
        private val rimuoviButton = view.findViewById<Button>(R.id.buttonRimuovi)

        fun bind(film : CarrelloFilm){
            val imageUrl = UrlFormatter.getMoviePosterUrl(film.titolo)
            Glide.with(context)
                .load(imageUrl)
                .error(R.drawable.mv_logo_light) // Immagine di fallback in caso di errore
                .into(filmImg)
            titoloText.text = film.titolo
            registaText.text = film.regista

            rimuoviButton.setOnClickListener {
                rimuoviDalCarrello(film)
            }
        }

        private fun rimuoviDalCarrello(film: CarrelloFilm) {
            val userId = SharedPrefManager.getUserId(context)

            if (userId != null) {
                val userCart = SharedPrefManager.getUserCart(userId, context)
                val filmsArray = userCart.optJSONArray("films") ?: JSONArray()
                val newArray = JSONArray()

                for (i in 0 until filmsArray.length()) { // Ricrea l'array senza il film da rimuovere
                    val currentFilm = filmsArray.getJSONObject(i)
                    val currentId = currentFilm.optInt("film_id")
                    Log.d("Carrello", "Confronto: ${film.filmId} == $currentId ?")
                    if (currentId != film.filmId) {
                        newArray.put(currentFilm)
                    } else {
                        Log.d("Carrello", "Film con id ${film.filmId} rimosso dal carrello")
                    }
                }
                userCart.put("films", newArray)
                SharedPrefManager.saveUserCart(userId, userCart.toString(), context)
                Log.d("Carrello", "carrello dopo la rimozione: ${SharedPrefManager.getUserCart(userId, context)}")

                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyChange(position)
                    Toast.makeText(context, "Film rimosso dal carrello", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

}
