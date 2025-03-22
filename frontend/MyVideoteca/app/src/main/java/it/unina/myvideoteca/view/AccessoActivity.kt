package it.unina.myvideoteca.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.unina.myvideoteca.MainActivity
import it.unina.myvideoteca.R
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import org.json.JSONObject

class AccessoActivity: AppCompatActivity() {
    private lateinit var serverController: ServerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accesso)

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val accediButton = findViewById<Button>(R.id.buttonAccedi)
        val registratiText = findViewById<TextView>(R.id.textRegistrati)

        serverController = ServerController(SocketSingleton.client)

        accediButton.setOnClickListener{
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Compilare tutti i campi per procedere.", Toast.LENGTH_SHORT).show()
            }else{
                accesso(email, password)
            }
        }

        registratiText.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun accesso(email: String, password: String){
        Thread {
            val response = serverController.logIn(email, password)
            runOnUiThread {
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        //TODO: alva i dati restituiti dal json (es: il numero max di noleggi)
                        val intent = Intent(this@AccessoActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                        Toast.makeText(this, "Accesso avvenuto con successo.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Connessione persa! Tentativo di riconnessione...", Toast.LENGTH_SHORT).show()
                    SocketSingleton.client.attemptReconnect()
                    if (SocketSingleton.client.isConnected()) { //se si è riusciti a riconnettersi al server si passa alla main activity (si torna da capo)
                        val intent = Intent(this@AccessoActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()  // Chiudi l'attività corrente (l'utente non può tornare "indietro" alla pagina corrente)
                    }
                }
            }
        }.start()
    }
}