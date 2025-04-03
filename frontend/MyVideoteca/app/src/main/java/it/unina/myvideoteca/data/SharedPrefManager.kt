package it.unina.myvideoteca.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

object SharedPrefManager {
    private const val PREF_NAME = "my_app_prefs"

    private const val USER_ID = "user_id"
    private const val NUM_NON_RESTITUITI = "num_non_restituiti"
    private const val MAX_NOLEGGI = "max_noleggi"
    private const val NON_RESTITUITI_BOOL = "non_restituiti_bool"
    private const val JWT_TOKEN = "jwt_token"

    private const val CARTS = "carts"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserId(context: Context, userId: String) {
        val editor = getPreferences(context).edit()
        editor.putString(USER_ID, userId)
        editor.apply()
    }

    fun getUserId(context: Context): String? {
        return getPreferences(context).getString(USER_ID, null)
    }

    fun saveNumNonRestituiti(context: Context, num: String) {
        val editor = getPreferences(context).edit()
        editor.putString(NUM_NON_RESTITUITI, num)
        editor.apply()
    }

    fun getNumNonRestituiti(context: Context): Int? {
        return getPreferences(context).getString(NUM_NON_RESTITUITI, null)?.toInt()
    }

    fun saveMaxNoleggi(context: Context, maxNoleggi: String) {
        val editor = getPreferences(context).edit()
        editor.putString(MAX_NOLEGGI, maxNoleggi)
        editor.apply()
    }

    fun getMaxNoleggi(context: Context): Int? {
        return getPreferences(context).getString(MAX_NOLEGGI, null)?.toInt()
    }

    fun saveNonRestituitiBool(context: Context, nonRestituiti: String) {
        val editor = getPreferences(context).edit()
        editor.putString(NON_RESTITUITI_BOOL, nonRestituiti)
        editor.apply()
    }

    fun getNonRestituitiBool(context: Context): Boolean {
        return getPreferences(context).getString(NON_RESTITUITI_BOOL, null).toBoolean()
    }

    fun saveToken(context: Context, token: String) {
        val editor = getPreferences(context).edit()
        editor.putString(JWT_TOKEN, token)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(JWT_TOKEN, null)
    }

    fun saveUserCart(userId: String?, cartJson: String, context: Context) {
        val carts = JSONObject(getPreferences(context).getString(CARTS, "{}") ?: "{}")
        if (userId != null) {
            carts.put(userId, JSONObject(cartJson))
            getPreferences(context).edit().putString(CARTS, carts.toString()).apply()
        }
    }

    fun getUserCart(userId: String?, context: Context): JSONObject {
        val carts = JSONObject(getPreferences(context).getString(CARTS, "{}") ?: "{}")
        return carts.optJSONObject(userId) ?: JSONObject()
    }

    fun logout(context: Context) {
        val editor = getPreferences(context).edit()
        editor.putString(USER_ID, "")
        editor.putString(NUM_NON_RESTITUITI, "")
        editor.putString(MAX_NOLEGGI, "")
        editor.putString(NON_RESTITUITI_BOOL, "")
        editor.putString(JWT_TOKEN, "")
        editor.apply()
    }
}