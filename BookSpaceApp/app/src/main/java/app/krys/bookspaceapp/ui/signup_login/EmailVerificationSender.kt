package app.krys.bookspaceapp.ui.signup_login

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseUser


class EmailVerificationSender(private var context: FragmentActivity) {

    fun send(newUser: FirebaseUser?) {
        // Send verification email
        newUser?.sendEmailVerification()?.addOnCompleteListener(context) { task ->
            if (task.isSuccessful) {
                Toast.makeText(context,
                    "Verification email sent to ${newUser.email}",
                    Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context,
                    "Unable to sent verification email",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}