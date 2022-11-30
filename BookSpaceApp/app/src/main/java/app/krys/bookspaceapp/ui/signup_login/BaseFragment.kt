package app.krys.bookspaceapp.ui.signup_login


import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar


open class BaseFragment : DialogFragment() {


    private var progressBar: ProgressBar? = null
    private var context: Activity? = null

    fun setProgressBar(bar: ProgressBar) {
        progressBar = bar
    }

    fun setToasterContext(_context: Activity?) {
        context = _context
    }

    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        if (progressBar?.visibility == View.VISIBLE)
            progressBar?.visibility = View.INVISIBLE
    }

    fun hideKeyboard(view: View) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun toastMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }


    fun snackBar(view: View, msg: String) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
    }

    fun doStringMatch(password: String, confirmPW: String): Boolean = password == confirmPW

}