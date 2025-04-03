package it.unina.myvideoteca.utils

object UrlFormatter {
    fun getMoviePosterUrl(titolo: String): String {
        val baseUrl = "https://raw.githubusercontent.com/itssabrinaa/Images-for-MyVideoteca/main/"
        val formattedTitle = titolo.replace(" ", "%20")
        return "$baseUrl$formattedTitle.jpg"
    }
}