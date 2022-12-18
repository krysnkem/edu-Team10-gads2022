package app.krys.bookspaceapp.ui.signup_login



import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.EmailVerificationDialogBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ResendEmailVerificationDialog : BaseFragment(), View.OnClickListener {

    private var _binding: EmailVerificationDialogBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var cancelButton: MaterialButton
    private lateinit var resendButton: MaterialButton
    private lateinit var _progressBar: ProgressBar
    private lateinit var contentContainer: LinearLayout
    private lateinit var verifyState: TextView

    private lateinit var auth: FirebaseAuth

    // Form validator
    private var formValidator: FormValidator? = null
    // Send email to new user for verification
    private var emailVerificationSender: EmailVerificationSender? = null

    // Required activity / context
    private lateinit var activityContext: Activity


    private fun DialogFragment.setUpWidthToMatchParent() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    // override fun getTheme(): Int = R.style.NoMarginDialog


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {

        _binding = EmailVerificationDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        emailField = binding.editTextEmail
        passwordField = binding.editTextEmail
        cancelButton = binding.cancelBtn
        resendButton = binding.resendEmailBtn
        _progressBar = binding.progressBar
        contentContainer = binding.contentContainer
        verifyState = binding.verifyState

        // Initialize for required activity
        activityContext = requireActivity()

        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setUpWidthToMatchParent()

        setProgressBar(_progressBar) // Set value
        setToasterContext(activityContext) // Set value

        auth = Firebase.auth // Initialize

        // Initialize form validator
        if (formValidator == null )
            formValidator = FormValidator(emailField, passwordField)

        if (emailVerificationSender == null)
            emailVerificationSender = EmailVerificationSender(requireActivity())

        initialButtonForClickAction()
    }


    /** ----------------------- DIALOG SETUP --------------------- */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.resend_email_btn -> resendEmailVerificationLink()
            R.id.cancel_btn -> closeFragment()
        }
    }


    private fun initialButtonForClickAction() {
        cancelButton.setOnClickListener(this)
        resendButton.setOnClickListener(this)
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
        hideKeyboard(contentContainer)

        resendButton.isEnabled = false

        /* Validate input fields
        * Check for empty string */
        if (formValidator != null) {
            if (!formValidator!!.validateForm()) {
                snackBar(contentContainer, getString(R.string.register_activity_field_isEmpty))
                resendButton.isEnabled = true
                return
            }
        }

        authenticateAndResendEmail(emailField.text.toString(), passwordField.text.toString())

        resendButton.isEnabled = true
    }



    private fun authenticateAndResendEmail(email: String, password: String) {

        showProgressBar()
        val credential = EmailAuthProvider.getCredential(email, password)

        auth.signInWithCredential(credential)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    // Send verification email to user's email address
                    emailVerificationSender?.send(auth.currentUser)
                    hideProgressBar()
                    auth.signOut()
                    closeFragment()
                }
        }.addOnFailureListener {
                toastMessage("Invalid Credentials.\n Please, try again")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}