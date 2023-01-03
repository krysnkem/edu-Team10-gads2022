package app.krys.bookspaceapp.ui.signup_login


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.FragmentLoginBinding
import com.firebase.ui.auth.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : BaseFragment(), View.OnClickListener {

    private val TAG = this::class.simpleName

    private var iItems: IItems? = null
    private lateinit var auth: FirebaseAuth
    private var authListener: FirebaseAuth.AuthStateListener? = null
    private lateinit var authUI: AuthUI


    private var _binding: FragmentLoginBinding? = null
    private val safeArgs: LoginFragmentArgs by navArgs()

    private lateinit var closeArrowBack: ImageButton

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setProgressBar(binding.progressBar)

        closeArrowBack = view.findViewById(R.id.close)

        // Initialize Firebase Auth
        auth = Firebase.auth
        authUI = AuthUI.getInstance()

        // Authentication state Listener
        setupFirebaseAuthStateCheck()

        initButtons()

         greetingMessage()
    }


    /** Initialize buttons */
    private fun initButtons() {
        binding.registerButton.setOnClickListener(this)
        binding.forgotPasswordButton.setOnClickListener(this)
        closeArrowBack.setOnClickListener(this)
        binding.loginButton.setOnClickListener(this)
        binding.resendEmailButton.setOnClickListener(this)
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

            if (user != null && signInProviderType) {
                if (user.isEmailVerified) {
                    // alternateButtonVisibility()
                    // Redirect User if authentication process is successful
                    findNavController().navigate(R.id.mainActivityDes, null)
                    requireActivity().finish()

                } else {
                    alternateButtonVisibility() // Alternate buttons visibility

                    Snackbar.make(requireView(),"Email verification is required!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK") {
                            emailNotVerified() // Show email verification dialog
                    }.show()

                    this.signIn()
                }
            }
        }
    }



    private fun signIn() {
        val email = binding.email
        val password = binding.enterPassword

        /* Validate input fields
        * Check for empty string */
        if (!validateForm(email, password)) {
            snackBar(requireView(), getString(R.string.register_activity_field_isEmpty))
            return
        }

        showProgressBar()
        hideKeyboard(requireView())

        auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(requireActivity()) {

                hideProgressBar()
                hideKeyboard(requireView())

            }.addOnFailureListener {

                toastMessage(getString(R.string.authentication_failure))
                hideProgressBar()

            }
    }



    /** Show message only if the user is signing up to use the App */
    private fun greetingMessage() {
        val isNewUserMessage = safeArgs.isNewUser
        val emailAddress = safeArgs.emailAddress
        // Log.d(TAG, "isNewUserMessage: $isNewUserMessage :: $emailAddress")
        if ((isNewUserMessage != "-1") && (emailAddress != "-1")) {  // Display greetings
            alternateButtonVisibility() // Alternate buttons visibility

            Snackbar.make(requireView(),
                    "$isNewUserMessage\n Email verification link sent to\n $emailAddress",
                    Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok") {
                    if (binding.registerButton.visibility == View.VISIBLE) alternateButtonVisibility()
                }.show()
        }
    }



    private fun alternateButtonVisibility() {
        binding.registerButton.visibility = View.GONE
        binding.resendEmailButton.visibility = View.VISIBLE
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
            R.id.register_button -> findNavController().navigate(R.id.action_loginFragment_to_signupFragment, null)
            R.id.forgot_password_button -> iItems!!.sendEmailResetPasswordLink(requireActivity())
            R.id.close -> iItems!!.onBackPressed()
            R.id.login_button -> signIn()
            R.id.resend_email_button -> emailNotVerified()
            R.id.facebook_button -> facebookSignProvider() // snackBar(requireView(), "Coming Soon!")
            R.id.twitter_button -> snackBar(requireView(), "Coming Soon!")
            R.id.google_button -> googleSignProvider()
        }
    }

}