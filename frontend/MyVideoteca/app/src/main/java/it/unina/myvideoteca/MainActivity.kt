package it.unina.myvideoteca

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import it.unina.myvideoteca.server.ServerController
import it.unina.myvideoteca.socket.SocketSingleton
import it.unina.myvideoteca.utils.RegexChecker
import it.unina.myvideoteca.view.AccessoActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var serverController: ServerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrazione)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registrazione)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets      /*Si adatta allo schermo del dispositivo in uso*/
        }

        val nomeEditText = findViewById<EditText>(R.id.editTextNome)
        val cognomeEditText = findViewById<EditText>(R.id.editTextCognome)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val erroriEmail = findViewById<TextView>(R.id.textErroriEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val erroriPassword = findViewById<TextView>(R.id.textErroriPassword)
        val registratiButton = findViewById<Button>(R.id.buttonRegistrati)
        val accediText = findViewById<TextView>(R.id.textAccedi)

        serverController = ServerController(SocketSingleton.client, this)

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*vuoto*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*vuoto*/ }
            override fun afterTextChanged(s: Editable?) {
                val mail = s.toString()
                val messaggiDiErrore = RegexChecker.verificaEmail(mail)

                if (messaggiDiErrore.isEmpty()) {
                    erroriEmail.visibility = TextView.GONE
                    registratiButton.isEnabled = true
                    registratiButton.backgroundTintList = getColorStateList(R.color.mv_purple)
                } else {
                    erroriEmail.visibility = TextView.VISIBLE
                    erroriEmail.text = messaggiDiErrore.joinToString("\n")
                    registratiButton.isEnabled = false
                    registratiButton.backgroundTintList = getColorStateList(android.R.color.darker_gray)
                }
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*vuoto*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*vuoto*/ }
            override fun afterTextChanged(s: Editable?) {
                val psw = s.toString()
                val messaggiDiErrore = RegexChecker.verificaPassword(psw)

                if (messaggiDiErrore.isEmpty()) {
                    erroriPassword.visibility = TextView.GONE
                    registratiButton.isEnabled = true
                    registratiButton.backgroundTintList = getColorStateList(R.color.mv_purple)
                } else {
                    erroriPassword.visibility = TextView.VISIBLE
                    erroriPassword.text = messaggiDiErrore.joinToString("\n")
                    registratiButton.isEnabled = false
                    registratiButton.backgroundTintList = getColorStateList(android.R.color.darker_gray)
                }
            }
        })

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

    private fun registrazione(nome: String, cognome: String, email: String, password: String) {
        serverController.signUp(nome, cognome, email, password) { response ->
            runOnUiThread {
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, "Registrazione avvenuta con successo.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, AccessoActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Connessione persa! Tentativo di riconnessione...", Toast.LENGTH_SHORT).show()
                    SocketSingleton.client.attemptReconnect()
                }
            }
        }
    }

}
