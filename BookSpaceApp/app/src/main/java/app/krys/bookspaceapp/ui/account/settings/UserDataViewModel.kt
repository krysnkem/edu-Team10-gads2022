package app.krys.bookspaceapp.ui.account.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.krys.bookspaceapp.ui.signup_login.User

class UserDataViewModel : ViewModel() {

    private val mutableUserdata = MutableLiveData<User?>()
    val userData: LiveData<User?> get() = mutableUserdata

    fun getUserData(user: User?) {
        mutableUserdata.value = user
    }
}