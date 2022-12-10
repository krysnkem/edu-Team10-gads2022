package app.krys.bookspaceapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.krys.bookspaceapp.databinding.ActivityMainBinding
import app.krys.bookspaceapp.ui.account.settings.AccountSettingsFragment
import app.krys.bookspaceapp.ui.signup_login.LoginViewModel
import app.krys.bookspaceapp.ui.signup_login.SignUpLoginActivity
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private val TAG = this::class.simpleName

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var binding: ActivityMainBinding
    private var accountSettingsFragment: AccountSettingsFragment? = null

    // Get a reference to the ViewModel scoped to this Fragment.
    private val viewModel by viewModels<LoginViewModel>()

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
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_account_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Click handler for item selection on drawer
//        navView.setNavigationItemSelectedListener { view ->
//            onNavigationItemSelected(view)
//        }


        // Initialize Fragment
        if (accountSettingsFragment == null) accountSettingsFragment = AccountSettingsFragment()

        if (navView.getHeaderView(0) != null) {
            val navHeader = navView.getHeaderView(0)
            val editProfileButton = navHeader.findViewById<ImageButton>(R.id.edit_button)
            editProfileButton.setOnClickListener {
                Toast.makeText(this, "Edit Profile!", Toast.LENGTH_LONG).show()
                inflateAccountSettingsFragment()
            }
        }

    }



//    private fun onNavigationItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            // R.id.nav_account_settings -> inflateAccountSettingsFragment()
//            R.id.nav_logout -> displayDialogLogoutConfirmation()
//        }
//        drawerLayout.closeDrawer(GravityCompat.START)
//        return true
//    }

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
            R.id.option_logout -> {
                displayDialogLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }




    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // super.onPrepareOptionsMenu(menu)
        observeAuthenticationState(menu)
        return true

    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


        private fun inflateAccountSettingsFragment() {
        if (accountSettingsFragment == null) accountSettingsFragment = AccountSettingsFragment()
        val transaction =  supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_account_settings, accountSettingsFragment!!, "FRAGMENT_ACC_SETTING")
        transaction.addToBackStack("FRAGMENT_ACC_SETTING")
        transaction.commit()
    }


    private fun displayDialogLogoutConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.confirm_action)
            .setPositiveButton(R.string.yes) { _, _ ->
                signOut()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.setTitle(getString(R.string.set_action_title))
        dialog.show()
    }


    private fun signOut() {
        authUI.signOut(this)
            .addOnCompleteListener {

                val intent = Intent(this, SignUpLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            }.addOnFailureListener {
                Log.d(TAG, "Unable to sign you out ${it.message}.")
            }
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
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout button and (2) display user details.
     * If there is no logged in user: show a login button
     */
    private fun observeAuthenticationState(menu: Menu) {

        viewModel.authenticationState.observe(this) { authenticationState ->

            val menuItemLogin: MenuItem = menu.findItem(R.id.option_login)
            val menuItemLogout: MenuItem = menu.findItem(R.id.option_logout)

            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {

                    menuItemLogin.isVisible = false
                    menuItemLogout.isVisible = true
                }
                else -> {

                    menuItemLogin.isVisible = true
                    menuItemLogout.isVisible = false
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