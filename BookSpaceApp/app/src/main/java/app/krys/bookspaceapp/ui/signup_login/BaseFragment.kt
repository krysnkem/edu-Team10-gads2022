package app.krys.bookspaceapp.ui.signup_login


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import app.krys.bookspaceapp.MainActivity
import app.krys.bookspaceapp.R
import com.firebase.ui.auth.*
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


open class BaseFragment : DialogFragment() {

    private val TAG = this::class.simpleName


    private var progressBar: ProgressBar? = null
    private var _signInProviderType: Boolean = true

    var signInProviderType: Boolean
    get() = _signInProviderType
    set(value) {
        _signInProviderType = value
    }

    fun setProgressBar(bar: ProgressBar) {
        progressBar = bar
    }


    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        if (progressBar?.visibility == View.VISIBLE)
            progressBar?.visibility = View.INVISIBLE
    }

    fun hideKeyboard(view: View) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun toastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }


    fun snackBar(view: View, msg: String) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
    }

    fun doStringMatch(password: String, confirmPW: String): Boolean = password == confirmPW


    /** Check whether log-in provider is twitter/facebook -- non password login method
     *  Or email -- password login method  */
    fun getSignInProvider(user: FirebaseUser): Boolean {
        val method = user.getIdToken(false).result.signInProvider
        return method.equals("password")
    }

    /** ----------------------- FirebaseUI Authentication -------------------------------------*/
    /** Helper immutable values for registering startActivityForResult to be Launched */
    private val activityResultForSignIn = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onActivityResult(result)
    }

    fun twitterSignProvider() {
        val providers = arrayListOf(AuthUI.IdpConfig.TwitterBuilder().build())
        authUiSignProviders(providers, R.id.twitter_button)
    }

    fun facebookSignProvider() {
        val providers = arrayListOf(AuthUI.IdpConfig.FacebookBuilder().build())
        authUiSignProviders(providers, R.id.facebook_button)
    }

    fun googleSignProvider() {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        authUiSignProviders(providers, R.id.google_button)
    }


    private fun authUiSignProviders(providers: ArrayList<AuthUI.IdpConfig>, buttonID: Int) {
        /**
         * You must provide a custom layout XML resource and configure at least one
         * provider button ID. It's important that that you set the button ID for every provider
         * that you have enabled.
         * com.firebase.ui.auth.R.layout.fui_idp_button_facebook */
        val customLayout = when (buttonID) {
            R.id.google_button -> {
                AuthMethodPickerLayout
                    .Builder(R.layout.fragment_login)
                    .setGoogleButtonId(R.id.google_button)
                    .build()
            }
            R.id.facebook_button -> {
                AuthMethodPickerLayout
                    .Builder(R.layout.fragment_login)
                    .setFacebookButtonId(R.id.facebook_button)
                    .build()
            }
            R.layout.fragment_login -> {
                AuthMethodPickerLayout
                    .Builder(R.layout.fragment_login)
                    .setTwitterButtonId(R.id.twitter_button)
                    .build()
            } else -> null
        }

        val signInIntent = customLayout?.let {
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAuthMethodPickerLayout(it) // customized with your own XML layout
                .setAvailableProviders(providers)
                //.setTheme(com.firebase.ui.auth.R.style.FirebaseUI_DefaultMaterialTheme)
                .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                .build()
        }

        _signInProviderType = false
        this.activityResultForSignIn.launch(signInIntent)
    }


    /** Helper function for registering startActivityForResult */
    private fun onActivityResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode == Activity.RESULT_OK) {
            // Log.d(TAG, "SOCIAL-LOGIN-DATA: ${Firebase.auth.currentUser} :: $signInProviderType")
            // Redirect User if authentication process is successful
             findNavController().navigate(R.id.mainActivityDes, null)
             requireActivity().finish()

        } else { // Failure: response.getError().getErrorCode() and handle error.
            // throw Exception("Error: ${response?.error?.errorCode}")
            // Sign in failed
            this.signOut()

            if (response == null) {
                // User pressed back button
                snackBar(requireView(),"Action Cancelled!")
                return
            }

            if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                snackBar(requireView(),"No Internet Connection!!")
                return
            }

            if (response.error?.message == EMAIL_TAKEN) {
                snackBar(requireView(),"This Email Address has been used.")
            }
            snackBar(requireView(),"Unknown Error Occurred")
            //Log.d(TAG, "Unknown Error Occurred: ${response.error}")
        }
    }





    /** Method that checks whether a user is registering as new user or login as an old user  */
    /*fun isNewUser(user: FirebaseUser): String {
        val metadata = user.metadata
        Log.d(TAG, "META-DATA: $metadata")
        val state = metadata!!.creationTimestamp == metadata.lastSignInTimestamp
        return if (state) "Thanks for signing up!!"  else "Welcome back!"
    }*/



    /** Method protects  every Activity or Fragment we want to protect being accessed
     * after the user is authenticated. finish() should be called at the end to
     * remove previous Activity from the stack to prevent user from going back to it
     * when back button is pressed */
    private fun redirectFromLoginScreenToHome() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }



    private fun signOut() {
        AuthUI.getInstance().signOut(requireContext())
            .addOnCompleteListener {
                Log.d(TAG, "Signed Out.")
            }.addOnFailureListener {
                Log.d(TAG, "Unable to sign you out ${it.message}.")
            }
    }


    fun validateForm(email: TextInputEditText,
                     password: TextInputEditText,
                     confirmPassword: TextInputEditText? = null): Boolean {

        var valid = true

        if (TextUtils.isEmpty(email.text.toString())) {
            email.error = "Required."
            valid = false
        } else {
            email.error = null
        }

        if (TextUtils.isEmpty(password.text.toString())) {
            password.error = "Required."
            valid = false
        } else {
            password.error = null
        }

        confirmPassword?.let {
            if (TextUtils.isEmpty(confirmPassword.text.toString())) {
                it.error = "Required."
                valid = false
            } else {
                it.error = null
            }
        }

        return valid
    }

    companion object {
        const val EMAIL_TAKEN = "Developer error"
    }

}