package it.unina.myvideoteca

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import it.unina.myvideoteca.view.AccessoActivity
import it.unina.myvideoteca.view.HomeActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var serverController: ServerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrazione)

        val nomeEditText = findViewById<EditText>(R.id.editTextNome)
        val cognomeEditText = findViewById<EditText>(R.id.editTextCognome)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val registratiButton = findViewById<Button>(R.id.buttonRegistrati)
        val accediText = findViewById<TextView>(R.id.textAccedi)

        serverController = ServerController(SocketSingleton.client)  // Usa il singleton

        registratiButton.setOnClickListener {
            val nome = nomeEditText.text.toString().trim()
            val cognome = cognomeEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Compilare tutti i campi per procedere.", Toast.LENGTH_SHORT).show()
            } else {
                registrazione(nome, cognome, email, password)
            }
        }

        accediText.setOnClickListener{
            val intent = Intent(this, AccessoActivity::class.java)
            startActivity(intent)
        }


    }

    private fun registrazione(nome: String, cognome: String, email: String, password: String){
        // Thread separato per evitare di bloccare la UI durante le operazioni
        Thread {
            val response = serverController.signUp(nome, cognome, email, password)
            runOnUiThread {
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        //TODO: Salva i dati restituiti dal json (es: il numero max di noleggi)
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                        Toast.makeText(this, "Registrazione avvenuta con successo.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Connessione persa! Tentativo di riconnessione...", Toast.LENGTH_SHORT).show()
                    SocketSingleton.client.attemptReconnect()
                }
            }
        }.start()
    }
}
