package app.krys.bookspaceapp.ui.signup_login

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null

    fun setProgressBar(bar: ProgressBar) {
        progressBar = bar
    }

    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        if (progressBar?.visibility == View.VISIBLE)
            progressBar?.visibility = View.INVISIBLE
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Return true lf user email ends with '@gmail.com'
    fun isValidDomain(email: String): Boolean =
        email.substring(email.indexOf("@") + 1).lowercase() == DOMAIN_NAME1 ||
                email.substring(email.indexOf("@") + 1).lowercase() == DOMAIN_NAME2



    fun doStringMatch(password: String, confirmPassword: String): Boolean = password == confirmPassword


    fun snackBar(view: View, msg: String) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
    }


    fun toastMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    public override fun onStop() {
        super.onStop()
        hideProgressBar()
    }


    companion object {
        private const val DOMAIN_NAME1 = "rocketmail.com"
        private const val DOMAIN_NAME2 = "gmail.com"
        private const val TAG = "UserRegistrationManager"
        private const val EMAIL_REGEX =
            "^[_A-Za-z0-9\\+]+(\\.[_A-Za-z0-9]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    }
}