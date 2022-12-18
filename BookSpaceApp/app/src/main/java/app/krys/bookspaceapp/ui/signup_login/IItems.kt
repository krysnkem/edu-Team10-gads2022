package app.krys.bookspaceapp.ui.signup_login

import androidx.fragment.app.FragmentActivity

interface IItems {

    fun showLoginFragment()

    fun hideLoginFragment()

    fun redirectFromLoginScreenToHome()
    fun exitAppFromLoginScreen()

    fun onBackPressed()

    fun inflateLoginFragment()

    fun inflateSignupFragment()

    fun showResendEmailVerificationDialog()

    fun sendEmailResetPasswordLink(fa: FragmentActivity)
}