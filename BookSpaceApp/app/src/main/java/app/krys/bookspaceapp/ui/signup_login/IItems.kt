package app.krys.bookspaceapp.ui.signup_login

import androidx.fragment.app.FragmentActivity

interface IItems {

    // fun redirectFromLoginScreenToHome()

    // fun exitAppFromLoginScreen()

    fun onBackPressed()

    fun showResendEmailVerificationDialog()

    fun sendEmailResetPasswordLink(fa: FragmentActivity)
}