package app.krys.bookspaceapp.ui.myspace

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import app.krys.bookspaceapp._util.createDialog
import app.krys.bookspaceapp._util.showToast
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.databinding.ConfirmOptionDialogLayoutBinding
import app.krys.bookspaceapp.databinding.FolderOptionsDialogLayoutBinding
import app.krys.bookspaceapp.databinding.FragmentCreateFolderBinding
import app.krys.bookspaceapp.databinding.FragmentMySpaceBinding
import app.krys.bookspaceapp.ui.adapter.myspace.FolderClickListener
import app.krys.bookspaceapp.ui.adapter.myspace.FolderOptionsClickListener
import app.krys.bookspaceapp.ui.adapter.myspace.MySpaceRecyclerViewAdpater
import app.krys.bookspaceapp.ui.home.HomeFragmentDirections
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference

/**
 * [MySpaceFragment] shows the users library space
 */
class MySpaceFragment : Fragment() {

    private var _binding: FragmentMySpaceBinding? = null
    private val binding get() = _binding!!

    lateinit var folderAdapter: MySpaceRecyclerViewAdpater

    private lateinit var createFolderDialog: AlertDialog
    private lateinit var createFolderDialogLayout: FragmentCreateFolderBinding

    private lateinit var folderOptionsDialog: AlertDialog
    private lateinit var folderOptionsDialogLayout: FolderOptionsDialogLayoutBinding

    private lateinit var confirmOptionDialog: AlertDialog
    private lateinit var confirmOptionDialogLayout: ConfirmOptionDialogLayoutBinding

    private val viewModel: MySpaceViewModel by activityViewModels {
        MySpaceViewModelFactory(requireActivity().application)
    }
    private lateinit var database: DatabaseReference
    private var user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMySpaceBinding.inflate(inflater, container, false)

        //inflate the dialogs layouts
        createFolderDialogLayout = FragmentCreateFolderBinding.inflate(inflater)
        folderOptionsDialogLayout = FolderOptionsDialogLayoutBinding.inflate(inflater)
        confirmOptionDialogLayout = ConfirmOptionDialogLayoutBinding.inflate(inflater)

        //create the Alert dialogs
        initializeAllAlertDialogs()
        return binding.root
    }

    private fun initializeAllAlertDialogs() {
        createFolderDialog = createDialog(
            createFolderDialogLayout.root,
            requireContext()
        )
        folderOptionsDialog = createDialog(
            folderOptionsDialogLayout.root,
            requireContext()
        )
        confirmOptionDialog = createDialog(
            confirmOptionDialogLayout.root,
            requireContext()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null)
            showLoadingOverlay()

        folderAdapter = MySpaceRecyclerViewAdpater(viewModel.options,
            FolderOptionsClickListener { folderInfo ->
                setUpDialogMethods(folderInfo)
            },
            FolderClickListener {folderInfo ->
                val action = HomeFragmentDirections.actionNavHomeToFolderContentFragment(folderInfo)
                findNavController().navigate(action)
            }
        )
        folderAdapter.dataChanged.observe(viewLifecycleOwner, Observer<Boolean>{
            hideLoadingOverlay()
        })



        binding.folderRv.adapter = folderAdapter

        binding.addFolderFab.setOnClickListener {
            createFolderDialogLayout.createBtn.isEnabled = false
            createFolderDialog.show()
        }

        binding.uploadFab.setOnClickListener {
            val action = HomeFragmentDirections.actionNavHomeToUploadBookFragment()
            Navigation.findNavController(view).navigate(action)
        }

        createFolderDialogLayout.createBtn.setOnClickListener {
            showLoadingOverlay()
            createFolder()
        }

        //check that characters entered for folder name is not more that 20 characters long
        createFolderDialogLayout.folderNameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            //enable creatBtn if the length of the text is between 6 and 20
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                createFolderDialogLayout.createBtn.isEnabled = p0?.length!! in 6..20
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun createFolder() {
        val folderName = createFolderDialogLayout.folderNameEt.text.toString()

        val task: Task<Void>? = viewModel.createFolder(folderName)
        if (task != null) {
            task.addOnSuccessListener {
                val text = "$folderName created"
                showToast(text, requireContext())
                hideLoadingOverlay()
            }
            task.addOnFailureListener {
                showToast(
                    "$folderName failed to create due to ${it.message}",
                    requireContext()
                )
                Log.e(
                    TAG,
                    "onViewCreated: $folderName failed to create due to ${it.message} "
                )
                hideLoadingOverlay()
            }
        } else {
            showToast("user is not signed in", requireContext())
            hideLoadingOverlay()
        }
        createFolderDialogLayout.folderNameEt.clearFocus()
        createFolderDialogLayout.folderNameEt.setText("")
        createFolderDialog.dismiss()
    }

    private fun setUpDialogMethods(folderInfo: FolderInfo) {
        // Show folder options
        folderOptionsDialog.show()

        //dismiss folder options dialog when about option is clicked
        folderOptionsDialogLayout.aboutOption.setOnClickListener {
            folderOptionsDialog.dismiss()
        }

        // when remove option is clicked, dismiss folder options dialog
        // and show confirm options dialog
        folderOptionsDialogLayout.removeOption.setOnClickListener {
            folderOptionsDialog.dismiss()
            confirmOptionDialog.show()

            //set the text of the confirm option dialog to show the folder name
            confirmOptionDialogLayout.confirmationTextTv.text =
                String.format("You are about to delete ${folderInfo.folderName}")

            //dismiss the confirm option dialog
            confirmOptionDialogLayout.cancelBtn.setOnClickListener {
                confirmOptionDialog.dismiss()
            }
            // execute the task of removing the folder
            confirmOptionDialogLayout.confimBtn.setOnClickListener {
                confirmOptionDialog.dismiss()
                showLoadingOverlay()

                val task = viewModel.removeFolder(folderInfo)
                if (task == null) {
//                    showToast("User is not signed in", requireContext())
                    hideLoadingOverlay()
                } else {
                    task
                        .addOnSuccessListener {
                            showToast("${folderInfo.folderName} removed!", requireContext())
                            hideLoadingOverlay()
                        }
                        .addOnFailureListener {
                            showToast(
                                "Unable to remove ${folderInfo.folderName} due to ${it.message}",
                                requireContext()
                            )
                            hideLoadingOverlay()
                        }
                }


            }
        }
    }

    override fun onStart() {
        super.onStart()
        folderAdapter.startListening()
    }

    override fun onPause() {
        super.onPause()
        folderAdapter.startListening()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private val TAG = this::class.simpleName
    }

    private fun hideLoadingOverlay() {
        binding.progressOverlay.visibility = View.INVISIBLE
    }

    private fun showLoadingOverlay() {
        binding.progressOverlay.visibility = View.VISIBLE
    }

}