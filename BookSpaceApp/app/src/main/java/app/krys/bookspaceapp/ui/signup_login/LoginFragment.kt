package app.krys.bookspaceapp.ui.signup_login


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.FragmentLoginBinding
import com.firebase.ui.auth.*
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase



class LoginFragment : BaseFragment(), View.OnClickListener {

    private val TAG = this::class.simpleName

    private var iItems: IItems? = null
    private lateinit var auth: FirebaseAuth
    private var authListener: FirebaseAuth.AuthStateListener? = null
    private lateinit var authUI: AuthUI
    // Form validator
    private var formValidator: FormValidator? = null
    // Send email to new user for verification
    private var emailVerificationSender: EmailVerificationSender? = null

    private var _binding: FragmentLoginBinding? = null
    private lateinit var closeArrowBack: ImageButton

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Get a reference to the ViewModel scoped to this Fragment.
    private val viewModel by viewModels<LoginViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))


        setProgressBar(binding.progressBar)

        closeArrowBack = view.findViewById(R.id.close)

        // Initialize Firebase Auth
        auth = Firebase.auth
        authUI = AuthUI.getInstance()


        // Authentication state Listener
        setupFirebaseAuthStateCheck()

        // Initialize form validator
        if (formValidator == null )
            with(binding) {
                formValidator = FormValidator(email,  enterPassword)
            }

        if (emailVerificationSender == null)
            emailVerificationSender = EmailVerificationSender(requireActivity())

        initButtons()
    }


    /** Initialize buttons */
    private fun initButtons() {
        binding.registerButton.setOnClickListener(this)
        closeArrowBack.setOnClickListener(this)
        binding.loginButton.setOnClickListener(this)
        binding.facebookButton.setOnClickListener(this)
        binding.twitterButton.setOnClickListener(this)
        binding.googleButton.setOnClickListener(this)
    }


    private fun emailNotVerified() {
        iItems!!.showResendEmailVerificationDialog()
    }


    private fun setupFirebaseAuthStateCheck() {

        authListener = FirebaseAuth.AuthStateListener {

            val user = auth.currentUser

            if (user != null) {

                if (user.isEmailVerified) {

                    // Redirect User if authentication process is successful
                    // snackBar(requireView(), "You are logged is ${user.email}")
                    iItems!!.redirectFromLoginScreenToHome()

                } else {

                    emailNotVerified()
                }
            }
        }
    }


    private fun signIn() {
        /* Validate input fields
        * Check for empty string */
        if (formValidator != null) {
            if (!formValidator!!.validateForm()) {
                snackBar(requireView(), getString(R.string.register_activity_field_isEmpty))
                return
            }
        }

        showProgressBar()
        hideKeyboard(requireView())

        val email = binding.email.text.toString()
        val password = binding.enterPassword.text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) {

                hideProgressBar()
                hideKeyboard(requireView())

            }.addOnFailureListener {

                toastMessage(getString(R.string.authentication_failure))
                hideProgressBar()

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


    override fun onStart() {
        super.onStart()
        //auth.addAuthStateListener(authListener!!)
        authListener?.let {
            auth.addAuthStateListener(it)
        }
    }


    override fun onStop() {
        super.onStop()
        authListener?.let {
            auth.removeAuthStateListener(it)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.register_button -> {
                iItems!!.inflateSignupFragment()
                iItems!!.hideLoginFragment()
            }
            R.id.close -> iItems!!.onBackPressed()
            R.id.login_button -> signIn()
            R.id.facebook_button -> snackBar(requireView(), "Coming Soon!")
            R.id.twitter_button -> snackBar(requireView(), "Coming Soon!")
            R.id.google_button -> this.authUiSignProviders()
        }
    }




    /** ----------------------- FirebaseUI Authentication ----------------*/
    // Helper immutable values for registering startActivityForResult to be Launched
    private val activityResultForSignIn = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onActivityResult(result)
    }


    private fun authUiSignProviders() {
        val providers = arrayListOf(
            //AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            // AuthUI.IdpConfig.FacebookBuilder().build()
        )

        /**
         * You must provide a custom layout XML resource and configure at least one
         * provider button ID. It's important that that you set the button ID for every provider
         * that you have enabled.
         * com.firebase.ui.auth.R.layout.fui_idp_button_facebook */
        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.fragment_login)
            .setGoogleButtonId(R.id.google_button)
            //.setFacebookButtonId(R.id.facebook_button)
            //.setTwitterButtonId(R.id.twitter_button)
            //.setTosAndPrivacyPolicyId(R.id.terms_and_conditions)
            .build()

        val signInIntent = authUI.createSignInIntentBuilder()
            .setAuthMethodPickerLayout(customLayout) // customized with your own XML layout
            .setAvailableProviders(providers)
            .setTheme(com.firebase.ui.auth.R.style.FirebaseUI_DefaultMaterialTheme)
            .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
            .build()

        this.activityResultForSignIn.launch(signInIntent)
    }


    // Helper function for registering startActivityForResult
    private fun onActivityResult(result: FirebaseAuthUIAuthenticationResult) {

        val response = result.idpResponse
        val user = auth.currentUser

        if (result.resultCode == Activity.RESULT_OK) {

            Log.d(TAG, "META-DATA2: ${user?.email} ")

            user?.let {
                if (this.getSignInProvider(user)) { // sign in with password and email
                    if (!user.isEmailVerified) { // Check if email has been verified
                        // Send verification email to user's email address
                        emailVerificationSender?.send(auth.currentUser)
                        // Sign out the user to force them to verify their account
                        this.signOut()
                    }

                }
            }

            // this.updateUser(user)

        } else { // Failure: response.getError().getErrorCode() and handle error.
            //throw Exception("Error: ${response?.error?.errorCode}")
            // Sign in failed
            user?.let {
                this.signOut()
            }

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


//    private fun isNewUser(user: FirebaseUser): Boolean {
//        val metadata = user.metadata
//        Log.d(TAG, "META-DATA: $metadata")
//        return metadata!!.creationTimestamp == metadata.lastSignInTimestamp
//
//    }


    /** Check whether log-in provider is twitter/facebook -- non password login method
     *  Or email -- password login method  */
    private fun getSignInProvider(user: FirebaseUser): Boolean {
        val method = user.getIdToken(false).result.signInProvider
        return method.equals("password")


    }


    private fun signOut() {
        authUI.signOut(requireContext())
            .addOnCompleteListener {
                Log.d(TAG, "Signed Out.")
            }.addOnFailureListener {
                Log.d(TAG, "Unable to sign you out ${it.message}.")
            }
    }


}