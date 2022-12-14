package app.krys.bookspaceapp.ui.signup_login


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import app.krys.bookspaceapp.MainActivity
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.FragmentLoginBinding
import com.firebase.ui.auth.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : BaseFragment(), View.OnClickListener {

    private val TAG = this::class.simpleName

    private var iItems: IItems? = null
    private lateinit var auth: FirebaseAuth
    private var authListener: FirebaseAuth.AuthStateListener? = null
    private lateinit var authUI: AuthUI
    //private var signInProviderType = true
    // Form validator
    private var formValidator: FormValidator? = null
    // Send email to new user for verification
    private var emailVerificationSender: EmailVerificationSender? = null


    private var _binding: FragmentLoginBinding? = null
    private lateinit var closeArrowBack: ImageButton

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


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
        binding.forgotPasswordButton.setOnClickListener(this)
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

            if (user != null && signInProviderType) {
                if (user.isEmailVerified) {
                    // Redirect User if authentication process is successful
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

                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)

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
            R.id.forgot_password_button -> iItems!!.sendEmailResetPasswordLink(requireActivity())
            R.id.close -> iItems!!.onBackPressed()
            R.id.login_button -> signIn()
            R.id.facebook_button -> snackBar(requireView(), "Coming Soon!")
            R.id.twitter_button -> snackBar(requireView(), "Coming Soon!")
            R.id.google_button -> googleSignProvider()
        }
    }

}