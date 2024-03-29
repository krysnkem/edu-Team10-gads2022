package app.krys.bookspaceapp.ui.account.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import app.krys.bookspaceapp.databinding.FragmentChangePhotoDialogBinding


class ChangePhotoDialog : DialogFragment() {

    private val TAG = this::class.simpleName


    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: FragmentChangePhotoDialogBinding? = null
    private val binding get() = _binding!!


    // Helper immutable values for registering startActivityForResult to be Launched
    private val activityResultForPhotoCamera = registerForActivityResult(
        ActivityResultContracts
            .StartActivityForResult()) {result ->
        onActivityResult(CAMERA_REQUEST_CODE, result)
    }

    private val  activityResultForChangePhoto = registerForActivityResult(
        ActivityResultContracts
            .StartActivityForResult()) {result ->
        onActivityResult(PICK_FILE_REQUEST_CODE, result)
    }

    // Using the activityViewModels() Kotlin property delegate from the
    // fragment-ktx artifact to retrieve the ViewModel in the activity scope
    private val imageSelectedViewModel by activityViewModels<PhotoDataViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentChangePhotoDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        //Initialize the textview for choosing an image from memory
        binding.dialogChoosePhoto.setOnClickListener {
            Log.d(TAG, "onClick: accessing phones memory.")

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            activityResultForChangePhoto.launch(intent)
        }

        //Initialize the textview for choosing an image from memory
        binding.dialogOpenCamera.setOnClickListener {
            Log.d(TAG, "onClick: starting camera")
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            activityResultForPhotoCamera.launch(cameraIntent)
        }

        return view
    }



    // Helper function for registering startActivityForResult
    private fun onActivityResult(requestCode: Int, result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_FILE_REQUEST_CODE -> {
                    val selectedImageUri = result.data?.data
                    Log.d(TAG,"onActivityResult: image: $selectedImageUri")

                    //send the bitmap and fragment to the interface
                    imageSelectedViewModel.getImagePath(selectedImageUri!!)
                    dialog!!.dismiss()
                }
                CAMERA_REQUEST_CODE -> {
                    Log.d(TAG, "onActivityResult: done taking a photo.")
                    val bitmap: Bitmap? = result.data?.extras!!["data"] as Bitmap?
                    imageSelectedViewModel.getImageBitmap(bitmap!!)
                    dialog!!.dismiss()
                }
                else -> dialog!!.dismiss()
            }
        }
    }



    companion object {
        const val CAMERA_REQUEST_CODE = 5467 //random number
        const val PICK_FILE_REQUEST_CODE = 8352 //random number
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}