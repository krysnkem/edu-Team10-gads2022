package app.krys.bookspaceapp.ui.signup_login


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
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
    /** Form validator */
    private var formValidator: FormValidator? = null
    /** Send email to new user for verification */
    private var emailVerificationSender: EmailVerificationSender? = null

    private var _binding: FragmentSignupBinding? = null

    private lateinit var closeArrowBack: ImageButton

    /** This property is only valid between onCreateView and
     * onDestroyView */
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        // Initialize form validator
        if (formValidator == null )
            formValidator = FormValidator(binding.email, binding.createPassword, binding.confirmPassword)


        if (emailVerificationSender == null)
            emailVerificationSender = EmailVerificationSender(requireActivity())


        initButtons()
    }

    /** Initialize buttons */
    private fun initButtons() {
        closeArrowBack.setOnClickListener(this)
        binding.registerButton.setOnClickListener(this)
    }


    /** Validated user provided data and if valid, call setupCreateAccount() method  */
    private fun createAccount() {
        this.hideKeyboard(requireView())

        /* Validate input fields
        * Check for empty string */
        if (formValidator != null) {
            if (!formValidator!!.validateForm()) {
                snackBar(requireView(), getString(R.string.register_activity_field_isEmpty))
                return
            }
        }

        binding.registerButton.isEnabled = false

        val email = binding.email.text.toString()
        val password = binding.createPassword.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        // Check whether password and confirm password fields match
        if (doStringMatch(password, confirmPassword)) {

            this.setupCreateAccount(email, password)

        } else {
            snackBar(requireView(), getString(R.string.register_activity_password_matching))
        }
        binding.registerButton.isEnabled = true
    }



    /** Send user data to database -- Real-time Database -- for Storage */
    private fun setupCreateAccount(email: String, password: String) {
        this.showProgressBar()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "OnComplete AuthState: ${auth.currentUser?.email}")
                    this.sendUserDataToDB(auth.currentUser)
                }
                if (!task.isSuccessful){
                    this.toastMessage("Unable to Register Account")
                    this.hideProgressBar()
                }
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
        }

        Log.d(TAG, "USER INFO: $user")

        val db = FirebaseDatabase.getInstance().reference
        db.child(getString(R.string.db_node_users))
            .child(currentUser!!.uid)
            .setValue(user)
            .addOnCompleteListener {

                val newUser = auth.currentUser

                // Sign user out after successful registration
                this.signOut()

                this.hideProgressBar()

                // Send verification email to user's email address
                this.emailVerificationSender?.send(newUser)

                // Redirect user to login screen to login
                this.redirectToLoginScreen()
            }
            .addOnFailureListener {
                this.hideProgressBar()
                this.toastMessage("Something Went Wrong!")
            }

    }


    /** Redirect user to Login Screen to log in after registration  */
    private fun redirectToLoginScreen() {
        iItems!!.onBackPressed()
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
        }
    }
}