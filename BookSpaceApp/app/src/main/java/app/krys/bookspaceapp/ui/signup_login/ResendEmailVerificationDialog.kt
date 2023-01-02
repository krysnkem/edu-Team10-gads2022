package app.krys.bookspaceapp.ui.signup_login



import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.EmailVerificationDialogBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ResendEmailVerificationDialog : BaseFragment(), View.OnClickListener {

    private val TAG = this::class.simpleName

    private var _binding: EmailVerificationDialogBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = EmailVerificationDialogBinding.inflate(inflater, container, false)

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setProgressBar(binding.progressBar) // Set value

        auth = Firebase.auth // Initialize

        initializeButtonForClickAction()
    }


    /** ----------------------- DIALOG SETUP --------------------- */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.resend_email_btn -> resendEmailVerificationLink()
            R.id.cancel_btn -> closeFragment()
        }
    }


    private fun initializeButtonForClickAction() {
        binding.cancelBtn.setOnClickListener(this)
        binding.resendEmailBtn.setOnClickListener(this)
    }


    private fun closeFragment() {
        dialog!!.dismiss()
    }


    override fun onResume() {
        super.onResume()

        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event?.action != KeyEvent.ACTION_DOWN) { // filter
                    closeFragment()
                    true
                } else {
                    // Hide your keyboard here
                    true
                }

            } else {
                // Pass on to be processed as normal
                false
            }
        }
    }


    override fun onStop() {
        super.onStop()
        hideProgressBar()
    }


    /** -------------------------FIREBASE SETUP --------------------- */
    private fun resendEmailVerificationLink () {
        hideKeyboard(requireView())
        binding.resendEmailBtn.isEnabled = false
        val email = binding.editTextEmail
        val password = binding.editTextPassword

        /* Validate input fields
        * Check for empty string */
        if (!validateForm(email, password)) {
            snackBar(requireView(), getString(R.string.register_activity_field_isEmpty))
            binding.resendEmailBtn.isEnabled = true
            return
        }
        authenticateAndResendEmail(email.text.toString(), password.text.toString())
    }



    private fun authenticateAndResendEmail(email: String, password: String) {
        showProgressBar()
        val credential = EmailAuthProvider.getCredential(email, password)

        auth.signInWithCredential(credential)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    binding.resendEmailBtn.isEnabled = true
                    // Send verification email to user's email address
                    val emailVerificationSender = EmailVerificationSender(requireActivity())
                    emailVerificationSender.send(auth.currentUser)
                    hideProgressBar()
                    auth.signOut()
                    closeFragment()
                }
        }.addOnFailureListener {
                snackBar(requireView(), "${it.message}\n Please, try again")
                binding.resendEmailBtn.isEnabled = true
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}