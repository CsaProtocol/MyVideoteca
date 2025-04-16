package it.unina.myvideoteca

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], manifest = Config.NONE)
class SignupTest {

    private lateinit var activity: MainActivity
    private lateinit var context: Context

    @Before
    fun setUp() {
        //Arrange
        context = ApplicationProvider.getApplicationContext()
        activity = spy(MainActivity())
        doNothing().`when`(activity).registrazione(anyString(), anyString(), anyString(), anyString())
        doNothing().`when`(activity).showToastCampiNonValidi()
    }

    @Test
    fun signUp_validInput_Test() {
        // Act
        activity.onRegistratiClicked(
            nome = "Mario",
            cognome = "Rossi",
            email = "mario.rossi@example.com",
            password = "Password123!"
        )

        // Assert
        verify(activity, times(1)).registrazione(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        verify(activity, never()).showToastCampiNonValidi()
    }

    @Test
    fun signUp_NoName_Test() {
        // Act
        activity.onRegistratiClicked(
            nome = "",
            cognome = "Rossi",
            email = "mario.rossi@example.com",
            password = "Password123!"
        )

        // Assert
        verify(activity, never()).registrazione(anyString(), anyString(), anyString(), anyString())
        verify(activity, times(1)).showToastCampiNonValidi()
    }

    @Test
    fun signUp_NoSurname_Test() {
        // Act
        activity.onRegistratiClicked(
            nome = "Mario",
            cognome = "",
            email = "mario.rossi@example.com",
            password = "Password123!"
        )

        // Assert
        verify(activity, never()).registrazione(anyString(), anyString(), anyString(), anyString())
        verify(activity, times(1)).showToastCampiNonValidi()
    }

    @Test
    fun signUp_NoEmail_Test() {
        // Act
        activity.onRegistratiClicked(
            nome = "Mario",
            cognome = "Rossi",
            email = "",
            password = "Password123!"
        )

        // Assert
        verify(activity, never()).registrazione(anyString(), anyString(), anyString(), anyString())
        verify(activity, times(1)).showToastCampiNonValidi()
    }

    @Test
    fun signUp_NoPassword_Test() {
        // Act
        activity.onRegistratiClicked(
            nome = "Mario",
            cognome = "Rossi",
            email = "mario.rossi@example.com",
            password = ""
        )

        // Assert
        verify(activity, never()).registrazione(anyString(), anyString(), anyString(), anyString())
        verify(activity, times(1)).showToastCampiNonValidi()
    }
}