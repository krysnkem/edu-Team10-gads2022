package app.krys.bookspaceapp.ui.signup_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

//    var signInProvider: Boolean? = null

    val _signInProvider = MutableLiveData<Boolean>()
    val signInProvider: LiveData<Boolean> = _signInProvider
    init {
        Firebase.auth.currentUser?.let { getSignInProvider(it) }
    }

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    /** Check whether log-in provider is twitter/facebook -- non password login method
     *  Or email -- password login method  */
    fun getSignInProvider(user: FirebaseUser) {
        viewModelScope.launch {
            try {
                _signInProvider.value = user.getIdToken(false).await().signInProvider.equals("password")
            } catch (e: FirebaseException) {
                e.printStackTrace()
            }

        }
    }
}