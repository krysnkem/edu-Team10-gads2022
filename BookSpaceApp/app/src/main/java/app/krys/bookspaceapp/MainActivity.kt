package app.krys.bookspaceapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.*
import androidx.navigation.ui.*
import app.krys.bookspaceapp._util.*
import app.krys.bookspaceapp.databinding.ActivityMainBinding
import app.krys.bookspaceapp.ui.account.settings.IUser
import app.krys.bookspaceapp.ui.account.settings.UserDataViewModel
import app.krys.bookspaceapp.ui.signup_login.LoginViewModel
import app.krys.bookspaceapp.ui.signup_login.SignUpLoginActivity
import app.krys.bookspaceapp.ui.signup_login.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : AppCompatActivity(), IUser {

    private val TAG = this::class.simpleName

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding

    // Get a reference to the ViewModel scoped to this Fragment.
    private val viewModel by viewModels<LoginViewModel>()
    private val userDataViewModel by viewModels<UserDataViewModel>()

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var authUI: AuthUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Initialize Firebase Auth
        auth = Firebase.auth
        authUI = AuthUI.getInstance()

        // Initialize view and drawer
        val navView: NavigationView = binding.navView
        drawerLayout = binding.drawerLayout
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_account_settings, R.id.nav_logout
            ), drawerLayout )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        createChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME)
        createDownloadChannel(DOWNLOAD_NOTIFICATION_CHANNEL_ID, DOWNLOAD_NOTIFICATION_CHANNEL_NAME)

        // Authentication state observer
        observeAuthenticationState(null)

        /** Manages nav header section  */
        navHeaderManager(navView)

    }




    private fun navHeaderManager(navView: NavigationView) {
        if (navView.getHeaderView(0) != null) {

            getUserAccountData()

            val navHeader = navView.getHeaderView(0)
            val editProfileButton = navHeader.findViewById<ImageButton>(R.id.edit_button)
            val name = navHeader.findViewById<TextView>(R.id.user_name)
            val email = navHeader.findViewById<TextView>(R.id.email)
            val profileImage = navHeader.findViewById<CircleImageView>(R.id.profile_image)

            // Get user data for nav header
            userDataViewModel.userData.observe(this) { user ->
                name.text = user?.name
                email.text = user?.email
                setProfileImage(user?.profile_image, profileImage)
            }

            // On click, display AccountSettingsFragment
            editProfileButton.setOnClickListener {
                navController.currentDestination?.let {
                    if (it.id != R.id.nav_account_settings) { // No navigation if it's the current destination
                        Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                            .navigate(R.id.nav_account_settings)
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

    }



    private fun setProfileImage(url: String?, profileImage: CircleImageView) {
        val defaultImage: Drawable? = ContextCompat.getDrawable(this, R.drawable.default_avatar)
        val requestOptions: RequestOptions = RequestOptions()
            .placeholder(defaultImage)

        Glide.with(this).setDefaultRequestOptions(requestOptions).load(url).into(profileImage)
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        return when (item.itemId) {
            R.id.option_login -> {
                val intent = Intent(this@MainActivity, SignUpLoginActivity::class.java)
                startActivity(intent)
                true
            }
            // Uses menu item to navigate to AccountSettingsFragment or LogoutDialogFragment
            else -> item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)}
    }




    /** REDUNDANT
     * observeAuthenticationState(menu: Menu?) and onResume() methods handle all
     * scenarios very well */
    /*override fun onStart() {
        super.onStart()
        checkAuthenticationState()
    }*/




    /** Hide Logout Menu item if the user is authenticated and
     * show login Menu item if the user is not authenticated, and vise-visa */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        observeAuthenticationState(menu)
        return true

    }




    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }




    private fun createChannel(channelId: String, channelName: String) {
        // TODO: Step 1.6 START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                    .apply {
                        enableLights(true)
                        lightColor = Color.RED
                        description = "Time for Breakfast"
                        setShowBadge(false)

                    }

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }






    /** -------- Get user data and use them to prefill input views in nav_header_main.xml ----------- */
    private fun getUserAccountData() {
        val db = FirebaseDatabase.getInstance().reference
        val user = auth.currentUser

        getSignInProvider(user!!)

        Log.d(TAG, "Method 2: Get User Data: ${user.providerData}")

        viewModel.signInProvider.observe(this) { signInProvider ->
            if (signInProvider) {
                var userData: User? = null
                val query: Query = db.child(getString(R.string.db_node_users))
                    .orderByKey()// .orderByValue() for a field value such as 'string'
                    .equalTo(user.uid)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        for (singleSnapshot: DataSnapshot in snapshot.children) {
                            userData = singleSnapshot.getValue<User>()
                            Log.d(TAG, "Method 1: Get User Data: ${userData.toString()}")
                        }
                        // Get user data and send the data to observe
                        userDataViewModel.getUserData(userData)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "ERROR FROM DB: ${error.details}")
                    }
                })

            } else {
                // Get user data and send the data to observe
                val userData = User(
                    name = user.displayName,
                    profile_image = user.photoUrl.toString(),
                    email = user.email
                )
                userDataViewModel.getUserData(userData)
            }
        }
    }



    private fun createDownloadChannel(channelId: String, channelName: String) {
        // TODO: Step 1.6 START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadNotificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                    .apply {
                        enableLights(true)
                        lightColor = ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.blue_accent
                        )
                        description = "Time for Breakfast"
                        setShowBadge(false)

                    }

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(downloadNotificationChannel)
        }
        // TODO: Step 1.6 END create a channel
    }



    override fun updateUserInfo() {
        getUserAccountData()
    }



    /** Check whether log-in provider is twitter/facebook -- non password login method
     *  Or email -- password login method  */
    private fun getSignInProvider(user: FirebaseUser) {
        viewModel.getSignInProvider(user)
    }


    /** Use to another layer of security to an app by calling it in the
     * onResume() method of every Activity or Fragment we want to protect being accessed
     * until the user is authenticated. finish() should be called at the end to
     * remove previous Activity from the stack to prevent user from going back to it
     * when back button is pressed */
    private fun checkAuthenticationState() {
        if (auth.currentUser == null) {
            val intent = Intent(this, SignUpLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }




    /**
     * Observes the authentication state and if the user is not authenticated,
     * redirect to Login Screen by **checkAuthenticationState()** method.
     */
    /**
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout button and (2) display user details.
     * If there is no logged in user: show a login button
     */
    private fun observeAuthenticationState(menu: Menu?) {

        viewModel.authenticationState.observe(this) { authenticationState ->

            val menuItemLogin: MenuItem? = menu?.findItem(R.id.option_login)
            val menuItemLogout: MenuItem? = menu?.findItem(R.id.nav_logout)

            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {

                    menuItemLogin?.isVisible = false
                    menuItemLogout?.isVisible = true

                }
                else -> {

                    menuItemLogin?.isVisible = true
                    menuItemLogout?.isVisible = false

                    clearAllCachedFiles(applicationContext)
                    checkAuthenticationState()
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        checkAuthenticationState()
    }


    // To Toggle drawer based on state when Back Button is pressed
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }


}