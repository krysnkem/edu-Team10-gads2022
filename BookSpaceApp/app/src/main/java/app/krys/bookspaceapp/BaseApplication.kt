package app.krys.bookspaceapp

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)

        // Create global configuration and initialize ImageLoader with this config

    }

}