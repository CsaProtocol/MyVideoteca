package it.unina.myvideoteca.utils

object RegexChecker {
    fun verificaPassword(password: String): List<String> {
        val errori = mutableListOf<String>()

        if (password.length < 8) {
            errori.add("La password deve essere di almeno 8 caratteri.")
        }
        if (!password.any { it.isUpperCase() }) {
            errori.add("La password deve contenere almeno un carattere maiuscolo.")
        }
        if (!password.any { it.isLowerCase() }) {
            errori.add("La password deve contenere almeno un carattere minuscolo.")
        }
        if (!password.any { it.isDigit() }) {
            errori.add("La password deve contenere almeno un numero.")
        }
        if (!password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?/".contains(it) }) {
            errori.add("La password deve contenere almeno un carattere speciale.")
        }

        return errori
    }

    fun verificaEmail(email: String): List<String> {
        val errori = mutableListOf<String>()
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

        if (email.isEmpty()) {
            errori.add("L'email non può essere vuota.")
        }
        if (!email.contains('@')) {
            errori.add("L'email deve contenere il simbolo '@'.")
        }
        if (!email.contains('.')) {
            errori.add("L'email deve contenere un punto ('.') nel dominio.")
        }
        if (!emailRegex.matches(email)) {
            errori.add("L'email non è valida.")
        }

        return errori
    }
}