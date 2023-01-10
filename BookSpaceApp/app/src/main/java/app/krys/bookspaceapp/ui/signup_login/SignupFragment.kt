package app.krys.bookspaceapp.ui.signup_login


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class SignupFragment : BaseFragment(), View.OnClickListener  {
    private val TAG = this::class.simpleName

    private var iItems: IItems? = null
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentSignupBinding? = null

    private lateinit var closeArrowBack: ImageButton

    /** This property is only valid between onCreateView and
     * onDestroyView */
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignupBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setProgressBar(binding.progressBar)

        closeArrowBack = view.findViewById(R.id.close)

        // Initialize Firebase Auth
        auth = Firebase.auth

        this.initButtons()
    }

    /** Initialize buttons */
    private fun initButtons() {
        closeArrowBack.setOnClickListener(this)
        binding.forgotPasswordButton.setOnClickListener(this)
        binding.registerButton.setOnClickListener(this)
        binding.facebookButton.setOnClickListener(this)
        binding.twitterButton.setOnClickListener(this)
        binding.googleButton.setOnClickListener(this)
    }


    /** Validated user provided data and if valid, call setupCreateAccount() method  */
    private fun createAccount() {
        this.hideKeyboard(requireView())

        val email = binding.email
        val password = binding.createPassword
        val confirmPassword = binding.confirmPassword

        /* Validate input fields
        * Check for empty string */
        if (!validateForm(email, password, confirmPassword)) {
            this.snackBar(requireView(), getString(R.string.register_activity_field_isEmpty))
            return
        }

        binding.registerButton.isEnabled = false

        // Check whether password and confirm password fields match
        if (doStringMatch(password.text.toString(), confirmPassword.text.toString())) {

            this.setupCreateAccount(email.text.toString(), password.text.toString())

        } else {
            this.snackBar(requireView(), getString(R.string.register_activity_password_matching))
        }
        binding.registerButton.isEnabled = true
    }



    /** Send user data to database -- Real-time Database -- for Storage */
    private fun setupCreateAccount(email: String, password: String) {
        this.showProgressBar()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) {
                // Log.d(TAG, "OnComplete AuthState: ${auth.currentUser?.email}")
                this.sendUserDataToDB(auth.currentUser)
            }.addOnFailureListener(requireActivity()) {
                this.snackBar(requireView(),"${it.message}")
                // Log.d(TAG, "setupCreateAccount: ${it.message} :: $signInProviderType")
                this.hideProgressBar()
            }

    }


    /** Helper function for setupCreateAccount() method */
    private fun sendUserDataToDB(currentUser: FirebaseUser?) {

        val user = currentUser?.let {
            val name = it.email?.substring(0, it.email!!.indexOf("@")) ?: ""
            val email =  it.email
            val profileImage = ""
            val userId = it.uid

            User(name, profileImage, email, userId)
        } ?: return // Prevents App from crashing if a user tries signing up twice with the same email

        // Log.d(TAG, "USER INFO: $user")

        val db = FirebaseDatabase.getInstance().reference
        db.child(getString(R.string.db_node_users))
            .child(currentUser.uid)
            .setValue(user)
            .addOnCompleteListener {
                // Sign user out after successful registration
                this.signOut()

                this.hideProgressBar()
                /** Send verification email to user's email address
                 * AND
                 * Redirect user to login screen to login */
                this.sendEmailVerificationLink(currentUser)
            }
            .addOnFailureListener {
                this.hideProgressBar()
                this.toastMessage("Something Went Wrong!")
            }

    }



    private fun sendEmailVerificationLink(newUser: FirebaseUser) {
        // Send verification email
        newUser.sendEmailVerification().addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                // Redirect user to login screen to login
                val isNewUser = "Thanks for signing up!!"
                val emailAddress = newUser.email
                val action = SignupFragmentDirections.actionSignupFragmentToLoginFragment(isNewUser, emailAddress!!)
                findNavController().navigate(action)
            } else {
                Toast.makeText(context,
                    "Unable to sent verification email",
                    Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun signOut() {
        auth.signOut()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            iItems = (context as IItems)
        } catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close -> iItems!!.onBackPressed()
            R.id.register_button -> createAccount()
            R.id.forgot_password_button -> iItems!!.sendEmailResetPasswordLink(requireActivity())
            R.id.facebook_button -> facebookSignProvider() // snackBar(requireView(), "Coming Soon!")
            R.id.twitter_button -> twitterSignProvider() //snackBar(requireView(), "Coming Soon!")
            R.id.google_button -> googleSignProvider()
        }
    }
}