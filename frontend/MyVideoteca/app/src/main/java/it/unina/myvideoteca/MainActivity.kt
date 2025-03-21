package it.unina.myvideoteca

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
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

        serverController = ServerController(SocketSingleton.client)  // Usa il singleton

        registratiButton.setOnClickListener {
            val nome = nomeEditText.text.toString().trim()
            val cognome = cognomeEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            registrazione(nome, cognome, email, password)
        }
    }

    private fun registrazione(nome: String, cognome: String, email: String, password: String){
        // Thread separato per inviare dati tramite il socket già connesso
        Thread {
            val response = serverController.registerUser(nome, cognome, email, password)
            runOnUiThread {
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        // Passa alla SuccessActivity
                        /*val intent = Intent(this@MainActivity, SuccessActivity::class.java)
                        startActivity(intent)
                        finish()*/
                        Toast.makeText(this, "Registrazione avvenuta con successo!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Errore durante la registrazione", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
