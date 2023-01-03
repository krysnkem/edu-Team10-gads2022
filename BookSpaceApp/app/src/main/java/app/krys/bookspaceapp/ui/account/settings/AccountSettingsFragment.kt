package app.krys.bookspaceapp.ui.account.settings


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.FragmentAccountSettingsBinding
import app.krys.bookspaceapp.ui.signup_login.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class AccountSettingsFragment : BaseFragment() {

    private val TAG = this::class.simpleName


    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var user: FirebaseUser? = null
    private lateinit var authUI: AuthUI

    private var iUser: IUser? = null

    //vars
    private lateinit var imageCompressor: ImageCompressor
    private var mStoragePermissions = false
    private var stateFlag = false
    private var mSelectedImageUri: Uri? = null
    private var mSelectedImageBitmap: Bitmap? = null
    private var progress = 0.0

    private lateinit var saveButton: MaterialButton
    private lateinit var resetPasswordButton: MaterialButton
    private lateinit var changePhotoButton: MaterialButton
    private lateinit var profileImage: CircleImageView
    private lateinit var userName: TextInputEditText
    private lateinit var email: TextInputEditText
    private lateinit var currentPassword: TextInputEditText
    private lateinit var _progressBar: ProgressBar
    private lateinit var cameraButton: ImageButton

    // Send email to new user for verification
    private var emailVerificationSender: EmailVerificationSender? = null

    // Get old values for input fields for username just like email
    private var oldUsername: String? = null

    // Get a reference to the ViewModel scoped to this Activity.
    private val viewModel by viewModels<LoginViewModel>()
    private val userDataViewModel by activityViewModels<UserDataViewModel>()
    // Using the activityViewModels() Kotlin property delegate from the
    // fragment-ktx artifact to retrieve the ViewModel in the activity scope
    private val imageSelectedViewModel by activityViewModels<PhotoDataViewModel>()


    private var _binding: FragmentAccountSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    /** For Fragment only: Checking permission in fragment for access to camera, file photos */
    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if (allAreGranted) {
                mStoragePermissions = true
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)

        // Initialize/launch camera or access to storage
        initPermissions()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize
        auth = Firebase.auth
        authUI = AuthUI.getInstance()
        user = auth.currentUser

        // User Authentication state observable
        observeAuthenticationState()

        // DB Init
        db = FirebaseDatabase.getInstance().reference

        // Cloud Storage Init
        storageReference = FirebaseStorage.getInstance().reference

        // Initialize Image compressor util
        imageCompressor = ImageCompressor(requireContext())


        if (emailVerificationSender == null)
            emailVerificationSender = EmailVerificationSender(requireActivity())


        initViewItems() // Call this method first to prevent NULL exception error
        init()

        setProgressBar(_progressBar)

        /** An Observables for photo or camera on selection */
        imageSelectedViewModel.imageBitmapSelected.observe(viewLifecycleOwner) { bitMap ->
            getImageBitmap(bitMap)
        }

        imageSelectedViewModel.imagePathSelected.observe(viewLifecycleOwner) { path ->
            getImagePath(path)
        }

    }




    private fun init() {
        getUserAccountData()

        resetPasswordButton.setOnClickListener {
            sendResetPasswordLink()
        }

        profileImage.setOnClickListener { // For click on avatar or profile image
            if (mStoragePermissions) {
                openDialogFragment()
            } else {
                verifyStoragePermissions()
            }
        }
        cameraButton.setOnClickListener { // For click on camera icon
            if (mStoragePermissions) {
                openDialogFragment()
            } else {
                verifyStoragePermissions()
            }
        }

        saveButton.setOnClickListener {
            // Change user data
            changeUserData()
        }

        changePhotoButtonState(false)
        changePhotoButton.setOnClickListener {
            if (stateFlag) {
                lifecycleScope.launch {
                    changePhotoButtonState(false)
                    performBackgroundTaskOnImageSize()
                }
            }
        }
    }



    private fun initViewItems() {
        binding.apply {
            saveButton = buttonSave
            resetPasswordButton = buttonChangePassword
            currentPassword = editTextPassword
            email = editTextEmail
            userName = editTextUsername
            changePhotoButton = buttonChangePhoto
            _progressBar = progressBar
        }
        profileImage = binding.profileImage
        cameraButton = binding.cameraButton
    }




    /** Request for permissions to access the user's storage or camera */
    private fun initPermissions() {
        if (!mStoragePermissions) {
            verifyStoragePermissions()
        }
    }




    /** Toggle button state from enabled to disabled depending whether user has taken action yet or not
     * DISABLED STATE: By default the button is disables
     * ENABLED STATE: The button is enabled once user selects photo or turns on the camera*/
    private fun changePhotoButtonState(flag: Boolean) {
        Log.d(TAG, "saveButtonState: Button state -> $flag")
        stateFlag = flag
        if (flag) {
            val color = ResourcesCompat.getColor(resources, R.color.blue_accent, requireContext().theme)
            changePhotoButton.setTextColor(color)
            changePhotoButton.isEnabled = true
        } else {
            changePhotoButton.setTextColor(Color.LTGRAY)
            changePhotoButton.isEnabled = false
        }
    }



    /** 1. Check whether log-in provider is twitter/facebook, that's, non password login method
     *  OR
     * 2  Email, that's, password login method.
     * If it's 1, deactivate all view items, but if it's 2, leave view items active for alteration */
    private fun onSocialLoginMethod() {
        user?.let {
            if (!getSignInProvider(it)) {

                val color = ResourcesCompat.getColor(resources, R.color.reader_text_color, requireContext().theme)

                // All buttons: Disabled and change text and background colors
                saveButton.setBackgroundColor(Color.LTGRAY)
                saveButton.setTextColor(color)
                profileImage.isEnabled = false
                saveButton.isEnabled = false
                // All buttons: Removed from the view
                cameraButton.visibility = View.GONE
                changePhotoButton.visibility = View.GONE
                resetPasswordButton.visibility = View.GONE
                // All Editable fields
                email.isEnabled = false
                userName.isEnabled = false
                currentPassword.setHintTextColor(ColorStateList.valueOf(color))
                currentPassword.isEnabled = false
            }
        }
    }




    /** Update user data in the DB calling other helper methods */
    private fun changeUserData() {
        showProgressBar()
        saveButton.isEnabled = false
        val emailAddress = email.text.toString()

        // If old and new emails are different, allow edit action
        if (!user?.email.equals(emailAddress)) {
            /* Validate input fields
            * Check for empty string */
            if (!TextUtils.isEmpty(email.text.toString()) &&
                !TextUtils.isEmpty(currentPassword.text.toString())) {

                editUserEmail()
            }

        }

        val newUsername = userName.text.toString()
        if ( !TextUtils.isEmpty(newUsername)) { // Change username
            if (oldUsername != newUsername)
                changeUsername()
        }

        iUser!!.updateUserInfo() // Update nav header

        saveButton.isEnabled = true
    }





    /** --------------- START: Change user data in the Database ---------- */
    private fun editUserEmail() {

        val emailAddress = email.text.toString()
        val password = currentPassword.text.toString()

        val credentials = user?.email?.let { email ->
            EmailAuthProvider.getCredential(email, password)
        }

        if (credentials != null) {
            user?.reauthenticate(credentials)
                ?.addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        // Check to see that the email is not already present in the DB
                        auth.fetchSignInMethodsForEmail(emailAddress)
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    val size = task2.result.signInMethods?.size
                                    if (size == 1 ) { // Returns 1 if email is in use
                                        hideProgressBar()
                                        toastMessage("The email supplied is in use.")
                                    } else {
                                        //toastMessage("Email is available for use.")
                                        // Add new Email
                                        user!!.updateEmail(emailAddress)
                                            .addOnCompleteListener { task3 ->

                                                if (task3.isSuccessful) {
                                                    toastMessage("Email Successfully Updated!")
                                                    emailVerificationSender?.send(user)
                                                    auth.signOut()
                                                } else {
                                                    toastMessage("Unable to update Email")
                                                }
                                                hideProgressBar()

                                            }.addOnFailureListener {
                                                hideProgressBar()
                                                toastMessage("Unable to update Email")
                                            }
                                    }
                                }
                            }.addOnFailureListener {
                                hideProgressBar()
                                toastMessage("Unable to update Email")
                            }
                    } else {
                        toastMessage("Incorrect credentials")
                        hideProgressBar()
                    }

                }?.addOnFailureListener {
                    hideProgressBar()
                    toastMessage("Unable to Update Email. Review details entered and try again")
                }
        }
    }



    private fun changeUsername() {
        db.child(getString(R.string.db_node_users))
            .child(user!!.uid)
            .child(getString(R.string.field_name))
            .setValue(userName.text.toString())
            .addOnCompleteListener {
                hideProgressBar()
            }.addOnFailureListener {
                hideProgressBar()
            }
    }


    private fun changeProfileImage(path: Uri) {
        db.child(getString(R.string.db_node_users))
            .child(auth.currentUser!!.uid)
            .child(getString(R.string.field_profile_image))
            .setValue(path.toString())
            .addOnCompleteListener {
                toastMessage("Upload Success")
                hideProgressBar()
                changePhotoButtonState(false)
                iUser!!.updateUserInfo() // Update nav header
            }.addOnFailureListener {
                // Handle any errors
                hideProgressBar()
                changePhotoButtonState(true)
                toastMessage("Error: Couldn't save image uri to DB")
            }
    }





    /** Reset password method */
    private fun sendResetPasswordLink() {
        user?.email?.let {
            auth.sendPasswordResetEmail(it)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toastMessage("Password reset link has been sent to your email.")
                        signOut()
                    } else {
                        toastMessage("Email Address Not Found!")
                    }

                }
        }
    }
    /** --------------- END: Change user data in the Database ---------- */




    private fun signOut() {
        authUI.signOut(requireActivity())
            .addOnCompleteListener {

                redirectToLoginScreen()

            }.addOnFailureListener {
                Log.d(TAG, "Unable to sign you out ${it.message}.")
            }
    }


    /** Redirect an unauthenticated user to Login screen for authentication */
    private fun redirectToLoginScreen() {
        val intent = Intent(requireActivity(), SignUpLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }




    /** Use to another layer of security to an app by calling it in the
     * onResume() method of every Activity or Fragment we want to protect being accessed
     * until the user is authenticated. finish() should be called at the end to
     * remove previous Activity from the stack to prevent user from going back to it
     * when back button is pressed */
    private fun checkAuthenticationState() {
        if (auth.currentUser == null) redirectToLoginScreen()
    }


    /**
     * Observes the authentication state and changes the UI accordingly.
     */
    private fun observeAuthenticationState() {

        viewModel.authenticationState.observe(this) { authenticationState ->

            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {

                    // Log.d(TAG, "SettingsActivity: ${user!!.email}: SUCCESSFULLY VERIFIED")
                    onSocialLoginMethod()

                }
                else -> {

                    checkAuthenticationState()

                }
            }
        }
    }






    /** -------- Get user data and use them to prefill input field displayed ----------- */
    private fun getUserAccountData() {

        iUser!!.updateUserInfo() // A callback to fetch updated data from DB

        // Observe data change and Update UI accordingly
        userDataViewModel.userData.observe(viewLifecycleOwner) { user ->
            // Get user data
            oldUsername = user?.name
            // Set user data field
            email.setText(user?.email)
            userName.setText(oldUsername)
            // imageLoader.displayImage(user?.profile_image, profileImage)
            setProfileImage(user?.profile_image)
        }
    }


    override fun onResume() {
        super.onResume()
        // Another layer of security to prevent an Unauthenticated
        // user from accessing this screen
        checkAuthenticationState()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            iUser = (context as IUser)
        } catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
        }
    }






    /** Get image path from user's storage when photo is selected */
    private fun getImagePath(imagePath: Uri?) {
        if (imagePath.toString() != "") {
            mSelectedImageBitmap = null
            mSelectedImageUri = imagePath
            changePhotoButtonState(true)
            Log.d(TAG, "getImagePath: got the image uri: $mSelectedImageUri")
            // on below line we are calling Glide and setting it
            // to display our image in our image view from image url
            setProfileImage(imagePath.toString())
        }
    }


    private fun setProfileImage(url: String?) {
        val defaultImage: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.default_avatar)
        val requestOptions: RequestOptions = RequestOptions()
            .placeholder(defaultImage)

        Glide.with(this).setDefaultRequestOptions(requestOptions).load(url).into(profileImage)
    }


    /** Get bitmap  from user's camera when photo is taken with their camera */
    private fun getImageBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            mSelectedImageUri = null
            mSelectedImageBitmap = bitmap
            changePhotoButtonState(true)
            Log.d(TAG, "getImageBitmap: got the image bitmap: $mSelectedImageBitmap")
            profileImage.setImageBitmap(bitmap) // Set image bitMap for image view
        }
    }


    /**
     * Compress profile photo for Firebase Storage using a @param ***imageUri*** OR @param ***bitmap***
     */
    private suspend fun performBackgroundTaskOnImageSize() {
        showProgressBar()
        toastMessage("compressing image")

        var deferred: Deferred<ByteArray?>? = null
        lifecycleScope.launch {
            try {

                if (mSelectedImageUri != null) {
                    deferred = async {
                        imageCompressor.doBackgroundWorkAsync(null, mSelectedImageUri)
                    }
                }
                if  (mSelectedImageBitmap != null) {
                    deferred = async {
                        imageCompressor.doBackgroundWorkAsync(mSelectedImageBitmap, null)
                    }
                }

                val mBytes: ByteArray? = deferred?.await()

                // hideProgressBar()
                Log.d(TAG, "RESULT: $mBytes")

                uploadProfileImageToStorage(mBytes)

            } catch (e: Exception) {

                hideProgressBar()
                Log.e(TAG, "ERROR: ${e.message.toString()}")
            }
        }
    }






    private fun uploadProfileImageToStorage(mBytes: ByteArray?) {
        val bytes: Int = mBytes?.size ?: return
        val fileName = "${FIREBASE_IMAGE_STORAGE}/${user!!.uid}/profile_image.jpg"
        Log.d(TAG, "uploadProfileImageToStorage: got the bitmap size: $bytes AND $mBytes")

        //specify where the photo will be stored
        val reference: StorageReference =  storageReference.child(fileName) //just replace the old image with the new one
        if ((bytes / MB) < MB_THRESH_HOLD) {

            // Create file metadata including the content type
            val metadata: StorageMetadata = StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setContentLanguage("en")
                // Custom metadata
                .setCustomMetadata("Application Name", "BookSpaceApp")
                .setCustomMetadata("location", "Nigeria")
                .build()

            //if the image size is valid then we can submit to database
            val uploadTask: UploadTask = reference.putBytes(mBytes, metadata)
            uploadTask.addOnProgressListener { taskSnapshot ->
                val currentProgress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toDouble()
                if (currentProgress > progress + 10) {
                    progress =  (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toDouble()
                    Log.d(TAG, "onProgress: Upload is $progress% done")
                    toastMessage("$progress%")
                }
            }.addOnFailureListener {
                // Handle unsuccessful uploads
                changePhotoButtonState(true)
                hideProgressBar()
                toastMessage("could not upload photo")
                Log.d(TAG, "uploadImageToFirebase: could not upload photo: $it")
            }.addOnSuccessListener { taskSnapshot ->
                // Upload on Successful, save image uri to database
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    changeProfileImage(uri)
                    Log.d(TAG, "uploadImageToFirebase: firebase download url : $uri")
                }
            }
        }
    }





    /**
     * Generalized method for asking permission. Can pass any array of permissions
     */
    private fun verifyStoragePermissions() {
        Log.d(TAG, "verifyPermissions: asking user for permissions.")
        val appPerms = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
        activityResultLauncher.launch(appPerms)
    }



    /** Change photo dialog method */
    private fun openDialogFragment() {
        val dialog = ChangePhotoDialog()
        val fragmentManager = requireActivity().supportFragmentManager
        dialog.show(fragmentManager, CHANGE_PHOTO_DIALOG)
        mStoragePermissions = false
    }



    companion object {
        private const val MB_THRESH_HOLD = 5.0
        private const val MB = 1000000.0
        private const val CHANGE_PHOTO_DIALOG = "Change_Photo_Dialog"
        private const val FIREBASE_IMAGE_STORAGE = "images/users"
    }




}