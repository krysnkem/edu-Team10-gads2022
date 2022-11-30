package app.krys.bookspaceapp.ui.signup_login


import android.text.TextUtils
import com.google.android.material.textfield.TextInputEditText


class FormValidator(private val emailField: TextInputEditText,
                    private val passwordField: TextInputEditText,
                    private var confirmPasswordField: TextInputEditText? = null,
                    /*private var nameField: TextInputEditText? = null,
                    private var phoneNumberField: TextInputEditText? = null,*/) {


    fun validateForm(): Boolean {

        var valid = true
        // registerButton.isEnabled = false

        val email = emailField.text.toString()
        if (TextUtils.isEmpty(email)) {
            emailField.error = "Required."
            valid = false
        } else {
            emailField.error = null
        }

        val password = passwordField.text.toString()
        if (TextUtils.isEmpty(password)) {
            passwordField.error = "Required."
            valid = false
        } else {
            passwordField.error = null
        }

        val confirmPassword = confirmPasswordField?.text.toString()
        confirmPasswordField?.let {
            if (TextUtils.isEmpty(confirmPassword)) {
                it.error = "Required."
                valid = false
            } else {
                it.error = null
            }
        }

        return valid
    }


}