package app.krys.bookspaceapp.ui.signup_login



import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import app.krys.bookspaceapp.BuildConfig
import app.krys.bookspaceapp.databinding.ActivitySignUpLoginBinding
import app.krys.bookspaceapp.ui.account.settings.ResetPasswordDialogFragment


class SignUpLoginActivity : AppCompatActivity(), IItems {

    private lateinit var binding: ActivitySignUpLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // keyHash()
        // enableStrictMode()
    }


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


    /** Watches for undesired operations, like long running operations, that might impact the MainThread
     * and enforce penalties */
    private fun enableStrictMode() {
        if (BuildConfig.DEBUG) {
            val policy = StrictMode.ThreadPolicy.Builder()
                /*.detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()*/
                .detectAll()
                /*.penaltyDialog()
                .penaltyDeath()*/ // Comes last in penalties chain
                .penaltyLog()
                .build()

            StrictMode.setThreadPolicy(policy)
        }
    }



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