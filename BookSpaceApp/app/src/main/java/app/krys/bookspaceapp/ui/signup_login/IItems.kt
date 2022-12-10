package app.krys.bookspaceapp.ui.signup_login

interface IItems {

    fun showLoginFragment()

    fun hideLoginFragment()

    fun redirectFromLoginScreenToHome()
    fun exitAppFromLoginScreen()

    fun onBackPressed()

    fun inflateLoginFragment()

    fun inflateSignupFragment()

    fun showResendEmailVerificationDialog()
}