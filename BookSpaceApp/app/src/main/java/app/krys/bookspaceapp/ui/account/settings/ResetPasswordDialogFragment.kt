package app.krys.bookspaceapp.ui.account.settings

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.FragmentResetPasswordDialogBinding
import app.krys.bookspaceapp.ui.signup_login.BaseFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ResetPasswordDialogFragment : BaseFragment(), View.OnClickListener  {

    private var _binding: FragmentResetPasswordDialogBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var emailField: TextInputEditText
    private lateinit var cancelButton: MaterialButton
    private lateinit var sendButton: MaterialButton
    private lateinit var _progressBar: ProgressBar
    // Required activity / context
    // private lateinit var activityContext: Activity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentResetPasswordDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        initView()

        // Initialize for required activity
        // activityContext = requireActivity()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setProgressBar(_progressBar) // Set value
        // setToasterContext(activityContext) // Set value

        initialButtonForClickAction()
    }

    /** ----------------------- DIALOG SETUP --------------------- */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.send_email_btn -> {
                hideKeyboard(requireView())
                sendEmailResetPasswordLink()
            }
            R.id.cancel_btn -> closeFragment()
        }
    }


    private fun sendEmailResetPasswordLink() {
        Firebase.auth.sendPasswordResetEmail(emailField.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    emailField.text!!.clear()
                    closeFragment()
                    toastMessage("Password reset link has been sent to your email.")
                } else {
                    toastMessage("Email Address Not Found!")
                }

            }
    }


    private fun initView() {
        binding.apply {
            emailField = editTextEmail
            cancelButton = cancelBtn
            sendButton = sendEmailBtn
            _progressBar = progressBar
        }
    }



    private fun initialButtonForClickAction() {
        cancelButton.setOnClickListener(this)
        sendButton.setOnClickListener(this)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}