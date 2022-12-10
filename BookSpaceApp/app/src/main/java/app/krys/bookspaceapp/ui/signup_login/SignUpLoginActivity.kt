package app.krys.bookspaceapp.ui.signup_login


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.krys.bookspaceapp.MainActivity
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.ActivitySignUpLoginBinding
import app.krys.bookspaceapp.ui.account.settings.UniversalImageLoader
import com.nostra13.universalimageloader.core.ImageLoader


class SignUpLoginActivity : AppCompatActivity(), IItems {

    private lateinit var binding: ActivitySignUpLoginBinding
    /** Email verification dialog */
    private var resendEmailVerificationDialog: ResendEmailVerificationDialog? = null

    /** Fragments */
    private var loginFragment: LoginFragment? = null
    private var signupFragment: SignupFragment? = null

    private var activeStateFlag = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** Initialize resend Email Verification Dialog */
        if (resendEmailVerificationDialog == null )
            resendEmailVerificationDialog = ResendEmailVerificationDialog()

        inflateLoginFragment()
        initImageLoader()


    }


    /**
     * init universal image loader. Done only once for the whole app.
     *  HomeActivity is the perfectly place for this scenario since it's the
     *  first UI the user sees after authentication
     */
    private fun initImageLoader() {
        val imageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(imageLoader.getConfig())
    }



    /** Login and SignUp Fragments management */
    override fun inflateLoginFragment() {
        if (loginFragment == null) loginFragment = LoginFragment()
        manageFragments(loginFragment, R.id.login_container, FRAGMENT_LOGIN)
    }


    override fun inflateSignupFragment() {
        activeStateFlag = true

        if (signupFragment == null) signupFragment = SignupFragment()
        manageFragments(signupFragment, R.id.signup_container, FRAGMENT_SIGNUP)
    }


    private fun manageFragments(fragment: Fragment?, layout: Int, fragment_id: String) {
        this.supportFragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
            replace(layout, fragment!!, fragment_id)
            addToBackStack(fragment_id)
            commit()
        }
    }


    /** Helper functions for Login and SignUp Fragments visibility. Called on onBackPress() */
    override fun showLoginFragment() {
        binding.loginContainer.visibility = View.VISIBLE
    }

    override fun hideLoginFragment() {
        binding.loginContainer.visibility = View.GONE
    }

    /** Method protects  every Activity or Fragment we want to protect being accessed
     * after the user is authenticated. finish() should be called at the end to
     * remove previous Activity from the stack to prevent user from going back to it
     * when back button is pressed */
    override fun redirectFromLoginScreenToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /** OnBack pressed when Login Screen is visible, exit the Application */
    override fun exitAppFromLoginScreen() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    /** After registration, redirect user to Login Screen and show resend email verification dialog --
     * showResendEmailVerificationDialog() -- should in case an initial email was not sent for some reason */
    override fun showResendEmailVerificationDialog() {
        resendEmailVerificationDialog!!.isCancelable = false
        val fragmentManager = this.supportFragmentManager
        if (fragmentManager.findFragmentByTag(RESEND_EMAIL) == null)
            resendEmailVerificationDialog!!.show(fragmentManager, RESEND_EMAIL)
    }



    override fun onBackPressed() {
        super.onBackPressed()
        if (activeStateFlag) {
            showLoginFragment()
            inflateLoginFragment()
            activeStateFlag = false
        } else {
            exitAppFromLoginScreen()
        }
    }



    companion object {
        private const val FRAGMENT_LOGIN = "FRAGMENT LOGIN"
        private const val FRAGMENT_SIGNUP = "FRAGMENT SIGNUP"
        private const val RESEND_EMAIL = "Resend Email verification link"
    }


}