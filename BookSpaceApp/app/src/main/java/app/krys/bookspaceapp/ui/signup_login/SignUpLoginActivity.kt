package app.krys.bookspaceapp.ui.signup_login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import app.krys.bookspaceapp.MainActivity
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.ActivitySignUpLoginBinding

class SignUpLoginActivity : AppCompatActivity(), IItems {

    private lateinit var binding: ActivitySignUpLoginBinding
    // Email verification dialog
    private var resendEmailVerificationDialog: ResendEmailVerificationDialog? = null

    // Fragments
    private var loginFragment: LoginFragment? = null
    private var signupFragment: SignupFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize resend Email Verification Dialog
        if (resendEmailVerificationDialog == null )
            resendEmailVerificationDialog = ResendEmailVerificationDialog()

        inflateLoginFragment()


    }



    override fun inflateLoginFragment() {
        if (loginFragment == null) loginFragment = LoginFragment()
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.login_container, loginFragment!!, FRAGMENT_LOGIN)
            addToBackStack(FRAGMENT_LOGIN)
            commit()
        }
    }


    // Helper function for LoginFragment visibility. Called on onBackPress()
    private fun correctLoginVisibility() {
        loginFragment?.let {
            if (it.isVisible) showLoginFragment() else hideLoginFragment()
        }
    }

    override fun showLoginFragment() {
         binding.loginContainer.visibility = View.VISIBLE
    }

    override fun hideLoginFragment() {
         binding.loginContainer.visibility = View.GONE
    }

    override fun redirectFromLoginScreenToHome() {
        startActivity(Intent(this@SignUpLoginActivity, MainActivity::class.java))
        finish()
    }

    override fun inflateSignupFragment() {
        if (signupFragment == null) signupFragment = SignupFragment()
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.signup_container, signupFragment!!, FRAGMENT_SIGNUP)
            addToBackStack(FRAGMENT_SIGNUP)
            commit()
        }
    }

    override fun showResendEmailVerificationDialog() {
        resendEmailVerificationDialog!!.isCancelable = false
        val fragmentManager = this.supportFragmentManager
        if (fragmentManager.findFragmentByTag(RESEND_EMAIL) == null)
            resendEmailVerificationDialog!!.show(fragmentManager, RESEND_EMAIL)
    }


    // To Toggle drawer based on state when Back Button is pressed
    override fun onBackPressed() {
        super.onBackPressed()
        correctLoginVisibility()
    }



    companion object {
        private const val FRAGMENT_LOGIN = "FRAGMENT LOGIN"
        private const val FRAGMENT_SIGNUP = "FRAGMENT SIGNUP"
        private const val RESEND_EMAIL = "Resend Email verification link"
    }



}