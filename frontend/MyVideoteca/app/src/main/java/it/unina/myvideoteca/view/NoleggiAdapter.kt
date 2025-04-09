package it.unina.myvideoteca.view

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.unina.myvideoteca.MainActivity
import it.unina.myvideoteca.R
import it.unina.myvideoteca.data.Noleggio
import it.unina.myvideoteca.data.SharedPrefManager
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import it.unina.myvideoteca.utils.UrlFormatter
import org.json.JSONObject

class NoleggiAdapter(private val noleggiList: MutableList<Noleggio>,
                      private val context: Context
): RecyclerView.Adapter<NoleggiAdapter.NoleggiViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoleggiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.noleggio_card, parent, false)
        return NoleggiViewHolder(view, context){ position ->
            noleggiList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onBindViewHolder(holder: NoleggiViewHolder, position: Int) {
        val film = noleggiList[position]
        holder.bind(film)
    }

    override fun getItemCount() = noleggiList.size

    class NoleggiViewHolder(
        view: View,
        private val context: Context,
        private val notifyChange: (Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val filmImg = view.findViewById<ImageView>(R.id.imgFilm)
        private val titoloText = view.findViewById<TextView>(R.id.textTitolo)
        private val registaText = view.findViewById<TextView>(R.id.textRegista)
        private val inizioNoleggio = view.findViewById<TextView>(R.id.inizioNoleggioText)
        private val scadenzaNoleggio = view.findViewById<TextView>(R.id.scadenzaNoleggioText)
        private val restituisciButton = view.findViewById<Button>(R.id.buttonRestituisci)

        fun bind(noleggio : Noleggio){
            val imageUrl = UrlFormatter.getMoviePosterUrl(noleggio.titoloFilm)
            Glide.with(context)
                .load(imageUrl)
                .error(R.drawable.mv_logo_light) // Immagine di fallback in caso di errore
                .into(filmImg)
            titoloText.text = noleggio.titoloFilm
            registaText.text = noleggio.registaFilm
            inizioNoleggio.text = context.getString(R.string.inizio_noleggio, noleggio.dataNoleggio.take(19))
            scadenzaNoleggio.text = context.getString(R.string.scadenza_noleggio, noleggio.dataScadenza.take(19))

            restituisciButton.setOnClickListener {
                restituisciNoleggio(noleggio)
            }
        }

        private fun restituisciNoleggio(noleggio: Noleggio) {
            val serverController = ServerController(SocketSingleton.client, context)

            serverController.restituisciNoleggio(noleggio) { response ->
                (context as? AppCompatActivity)?.runOnUiThread {
                    if (response == null) {
                        gestisciConnessionePersa()
                        return@runOnUiThread
                    }

                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optString("status")
                    if (status == "success") {
                        rimuoviNoleggio()
                        Toast.makeText(context, "Noleggio restituito con successo.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        private fun rimuoviNoleggio() {
            val numNonRestituiti = SharedPrefManager.getNumNonRestituiti(context) ?: 0
            SharedPrefManager.saveNumNonRestituiti(context, (numNonRestituiti-1).toString())
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                notifyChange(position)
            }
        }

        private fun gestisciConnessionePersa() {
            Toast.makeText(context, "Connessione persa! Tentativo di riconnessione...", Toast.LENGTH_LONG).show()
            SocketSingleton.client.attemptReconnect()

            if (SocketSingleton.client.isConnected()) {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                (context as? AppCompatActivity)?.finish()
            }
        }

    }

}