package app.krys.bookspaceapp.ui.signup_login


import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import app.krys.bookspaceapp.R
import com.firebase.ui.auth.*
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


open class BaseFragment : DialogFragment() {

    private val TAG = this::class.simpleName


    private var progressBar: ProgressBar? = null
    private var context: Activity? = null
    private var iItems: IItems? = null
    private var _signInProviderType: Boolean = true

    var signInProviderType: Boolean
    get() = _signInProviderType
    set(value) {
        _signInProviderType = value
    }

    fun setProgressBar(bar: ProgressBar) {
        progressBar = bar
    }

    fun setToasterContext(_context: Activity?) {
        context = _context
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

        this.activityResultForSignIn.launch(signInIntent)
    }


    /** Helper function for registering startActivityForResult */
    private fun onActivityResult(result: FirebaseAuthUIAuthenticationResult) {

        val response = result.idpResponse

        if (result.resultCode == Activity.RESULT_OK) {

            val user = Firebase.auth.currentUser

            signInProviderType = false
            Log.d(TAG, "SOCIAL-LOGIN-DATA: $user")
            /** IF a user is registering for the first time, display one message and Redirect User
             * to Home Screen if authentication process is successful.
             * ELSE display another message and Redirect old user to Home Screen on successful login */
            val msg = if (isNewUser(user!!)) "Thanks for signing up!!"  else "Welcome back!"

            snackBar(requireView(), msg)
            iItems!!.redirectFromLoginScreenToHome()

        } else { // Failure: response.getError().getErrorCode() and handle error.
            //throw Exception("Error: ${response?.error?.errorCode}")
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

            snackBar(requireView(),"Unknown Error Occurred!")
            Log.d(TAG, "Unknown Error Occurred: ${response.error}")
        }
    }



    /** Method that checks whether a user is registering/login for the first time  */
    private fun isNewUser(user: FirebaseUser): Boolean {
        val metadata = user.metadata
        Log.d(TAG, "META-DATA: $metadata")
        return metadata!!.creationTimestamp == metadata.lastSignInTimestamp

    }




    private fun signOut() {
        AuthUI.getInstance().signOut(requireContext())
            .addOnCompleteListener {
                Log.d(TAG, "Signed Out.")
            }.addOnFailureListener {
                Log.d(TAG, "Unable to sign you out ${it.message}.")
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            iItems = (context as IItems)
        } catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
        }
    }

}