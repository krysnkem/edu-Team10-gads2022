package app.krys.bookspaceapp.ui.space_folder

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.FragmentCreateFolderBinding


/**
 * Use the [CreateFolderFragment] for creating folders in the user's space
 */
class CreateFolderFragment : DialogFragment() {
    private var _binding: FragmentCreateFolderBinding? = null
    private val binding: FragmentCreateFolderBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateFolderBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return activity?.let {
//            val builder = AlertDialog.Builder(it)
//            val inflater = requireActivity().layoutInflater
//            builder.setView(inflater.inflate(R.layout.fragment_create_folder, null))
//
//            builder.create()
//        }?: throw IllegalStateException("Activity cannot be null")
//    }
    companion object{
        const val TAG = "CreateFolderFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createBtn.setOnClickListener {
            dialog?.dismiss()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}