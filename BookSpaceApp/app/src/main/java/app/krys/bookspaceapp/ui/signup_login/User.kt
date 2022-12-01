package app.krys.bookspaceapp.ui.signup_login

data class User(val name: String? = null,
                val profile_image: String? = null,
                val email: String? = null,
                val user_id: String? = null) {


    override fun toString(): String {
        return "User(name='$name', profile_image='$profile_image', email='$email', user_id='$user_id')"
    }
}
