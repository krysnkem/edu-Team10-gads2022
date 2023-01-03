package app.krys.bookspaceapp.ui.signup_login


import android.os.Bundle
<<<<<<< HEAD
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
=======
import androidx.appcompat.app.AppCompatActivity
>>>>>>> origin/user-auth-refactored
import androidx.fragment.app.FragmentActivity
import app.krys.bookspaceapp.databinding.ActivitySignUpLoginBinding
import app.krys.bookspaceapp.ui.account.settings.ResetPasswordDialogFragment


class SignUpLoginActivity : AppCompatActivity(), IItems {

    private lateinit var binding: ActivitySignUpLoginBinding
<<<<<<< HEAD

    /** Email verification and Reset Password dialogs */
    private var resendEmailVerificationDialog: ResendEmailVerificationDialog? = null
    private var sendEmailResetPasswordLinkDialog: ResetPasswordDialogFragment? = null

    /** Fragments */
    private var loginFragment: LoginFragment? = null
    private var signupFragment: SignupFragment? = null

    private var activeStateFlag = false
=======
>>>>>>> origin/user-auth-refactored

    private val TIMEOUT = 3000L
    private var keepOn = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        binding = ActivitySignUpLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
<<<<<<< HEAD
        splashScreen.setKeepOnScreenCondition {
            Handler(Looper.getMainLooper()).postDelayed({
                keepOn = false
            }, TIMEOUT)
            keepOn
        }
        /** Initialize resend Email Verification Dialog */
        if (resendEmailVerificationDialog == null)
            resendEmailVerificationDialog = ResendEmailVerificationDialog()

        if (sendEmailResetPasswordLinkDialog == null)
            sendEmailResetPasswordLinkDialog = ResetPasswordDialogFragment()
=======

        // keyHash()
    }

>>>>>>> origin/user-auth-refactored

    /**@SuppressLint("PackageManagerGetSignatures")
    private fun keyHash() { // Generates Key Hash for Facebook Login
        // Add code to print out the key hash
        // Add code to print out the key hash
        try {
            val info = packageManager.getPackageInfo(
                applicationContext.packageName,
                PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("SignUpLoginActivity", "KeyHash: ${Base64.encodeToString(md.digest(), Base64.DEFAULT)}")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("SignUpLoginActivity","Name not found ${e.message} ::: $e")
        } catch (e: NoSuchAlgorithmException) {
            Log.d("SignUpLoginActivity","Error ${e.message} ::: $e")
        }
    }*/



    /** START: Reset Password Section */
    override fun sendEmailResetPasswordLink(fa: FragmentActivity) {
        sendEmailResetPasswordLinkDialog(fa)
    }

    private fun sendEmailResetPasswordLinkDialog(fa: FragmentActivity) {
        val fragmentManager = fa.supportFragmentManager
        if (fragmentManager.findFragmentByTag(SEND_RESET_PASSWORD_EMAIL) == null) {
            val sendEmailResetPasswordLinkDialog = ResetPasswordDialogFragment()
            sendEmailResetPasswordLinkDialog.isCancelable = false
            sendEmailResetPasswordLinkDialog.show(fragmentManager, SEND_RESET_PASSWORD_EMAIL)
        }
    }
    /** END: Reset Password Section */


<<<<<<< HEAD
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

=======
>>>>>>> origin/user-auth-refactored




    /** Method protects  every Activity or Fragment we want to protect being accessed
     * after the user is authenticated. finish() should be called at the end to
     * remove previous Activity from the stack to prevent user from going back to it
     * when back button is pressed */
    /*override fun redirectFromLoginScreenToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }*/



    /** OnBackPressed, exit the Application */
    /*override fun exitAppFromLoginScreen() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }*/



    /** After registration, redirect user to Login Screen and show resend email verification dialog --
     * showResendEmailVerificationDialog() -- should in case an initial email was not sent for some reason */
    override fun showResendEmailVerificationDialog() {
        val fragmentManager = this.supportFragmentManager
        if (fragmentManager.findFragmentByTag(RESEND_EMAIL) == null) {
            val resendEmailVerificationDialog = ResendEmailVerificationDialog()
            resendEmailVerificationDialog.isCancelable = false
            resendEmailVerificationDialog.show(fragmentManager, RESEND_EMAIL)
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }


    companion object {
        private const val RESEND_EMAIL = "Resend Email verification link"
        private const val SEND_RESET_PASSWORD_EMAIL = "send Email Reset Password link"
    }


}