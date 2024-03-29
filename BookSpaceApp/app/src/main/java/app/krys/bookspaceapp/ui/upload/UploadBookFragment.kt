package app.krys.bookspaceapp.ui.upload

import android.Manifest
import android.R
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import app.krys.bookspaceapp._util.createDialog
import app.krys.bookspaceapp._util.showToast
import app.krys.bookspaceapp.data.model.BookMetaData
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.databinding.FragmentCreateFolderBinding
import app.krys.bookspaceapp.databinding.FragmentUploadBookBinding
import app.krys.bookspaceapp.ui.adapter.metadata.BookMetaDataAdapter
import com.google.android.gms.tasks.Task


/**
 * [UploadBookFragment] handles the books upload screen and functionality
 */
class UploadBookFragment : Fragment() {
    private var _binding: FragmentUploadBookBinding? = null
    private val binding get() = _binding!!

    private var readPermissionGranted = false

    private val bookMetaDataList = mutableListOf<BookMetaData>()
    private var selectedFolderOption: FolderInfo? = null

    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var metaDataAdapter: BookMetaDataAdapter

    val folders = mutableListOf<String>()
    val folderInfoList = mutableListOf<FolderInfo>()

    private lateinit var createFolderDialog: AlertDialog
    private lateinit var createFolderDialogLayout: FragmentCreateFolderBinding

    private val viewModel by activityViewModels<UploadBookViewModel> {
        UploadBookViewModelFactory(requireActivity().application)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        createFolderDialogLayout = FragmentCreateFolderBinding.inflate(inflater)
        _binding = FragmentUploadBookBinding.inflate(inflater, container, false)

        createFolderDialog = createDialog(
            createFolderDialogLayout.root,
            requireContext()
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupArrayAdapter()
        setUpSpinner()

        binding.selectBtn.setOnClickListener {
            val storagePerssmion = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (storagePerssmion != PackageManager.PERMISSION_GRANTED) {
                requestWritePermission()
            } else {
//                chooseFile()
                selectFiles()
            }
        }
        binding.addFolderBtn.setOnClickListener {
            setUpDialogMethods()
        }
        setUpAllObservers()
    }

    private fun setUpDialogMethods() {
        createFolderDialog.show()
        createFolderDialogLayout.createBtn.setOnClickListener {
            createFolder()
        }
    }

    private fun setUpSpinner() {
        binding.spinner.adapter = arrayAdapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
                showToast(
                    "Folder Selected: ${folderInfoList[position].folderName}", requireContext()
                )
                selectedFolderOption = folderInfoList[position]
                viewModel.setSelectedItem(selectedFolderOption!!)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }
    }

    private fun setupArrayAdapter() {
        arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            folders
        )
            .apply {
                setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            }
    }

//    private fun chooseFile() {
//        selectPdfFileContract.launch("application/pdf")
//    }

    private fun selectFiles() {
        selectMulitplePdfFileContract.launch("application/pdf")
    }

    private val filePermissionContract: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            readPermissionGranted = it
        }

    //    private val selectPdfFileContract: ActivityResultLauncher<String> =
//        registerForActivityResult(ActivityResultContracts.GetContent()) {
//            it?.let {
//                showLoadingOverlay()
//                bookUri = it
//                viewModel.loadBookFile(it)
//            }
//
//        }
    private val selectMulitplePdfFileContract: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            it?.let {
                showLoadingOverlay()
                viewModel.loadAllBookFiles(it)
            }

        }


    private fun requestWritePermission() {
        filePermissionContract.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun hideLoadingOverlay() {
        binding.progressOverlay.visibility = View.INVISIBLE
    }

    private fun showLoadingOverlay() {
        binding.progressOverlay.visibility = View.VISIBLE
    }

    private fun setUpAllObservers() {
        viewModel.folderNamesList.observe(
            viewLifecycleOwner,
            Observer<List<String>> { folderNameList ->
                folders.clear()
                folders.addAll(folderNameList)
                arrayAdapter.notifyDataSetChanged()
            })

        viewModel.folderInfoList.observe(
            viewLifecycleOwner,
            Observer<List<FolderInfo>> { infoList ->
                folderInfoList.clear()
                folderInfoList.addAll(infoList)
            })

        viewModel.selectedFolderOption.observe(viewLifecycleOwner, Observer {
            selectedFolderOption = it
            binding.spinner.setSelection(folderInfoList.indexOf(selectedFolderOption))
        })

//        viewModel.metadata.observe(viewLifecycleOwner, Observer<BookMetaData> { data ->
//            if (!data.isEmpty()) {
//                bookMetatData = data
//                binding.viewBookLayout.bookData = data
//                binding.bookNameTv.text = data.fileName
//                viewModel.saveBitmapToFile(data.frontPage!!)
//
//                hideLoadingOverlay()
//                binding.viewBookLayout.cardView.visibility = View.VISIBLE
//                binding.uploadBtn.isEnabled = true
//                Toast.makeText(requireContext(), "$data", Toast.LENGTH_LONG).show()
//            } else {
//                binding.viewBookLayout.cardView.visibility = View.GONE
//                binding.uploadBtn.isEnabled = false
//                binding.bookNameTv.text = ""
//            }
//
//            binding.uploadBtn.setOnClickListener {
//                if (selectedFolderOption != null) {
//                    viewModel.createFileUploadWorkRequests(
//                        data,
//                        selectedFolderOption!!.folderId!!
//                    ).state.observe(viewLifecycleOwner, Observer { state ->
//                        if (state is Operation.State.IN_PROGRESS) {
//                            viewModel.resetState()
//                        }
//                    })
//                }
//            }
//        })

        viewModel.metadataList.observe(viewLifecycleOwner, Observer { metaDataList ->
            hideLoadingOverlay()
            metaDataAdapter = BookMetaDataAdapter()
            binding.booksRv.adapter = metaDataAdapter
            if (metaDataList.isNotEmpty()) {
                bookMetaDataList.clear()
                bookMetaDataList.addAll(metaDataList)


                metaDataAdapter.submitList(metaDataList)
                binding.uploadBtn.isEnabled = true


                binding.uploadBtn.setOnClickListener {
                    showLoadingOverlay()
                    if (selectedFolderOption != null) {
//                        metaDataList.forEach { metaData ->
//                            viewModel.saveBitmapToFile(metaData.frontPage!!)
//                        }
                        viewModel.saveAllBookCoverBitmapToFile(metaDataList)


                    }
                }

//                metaDataList.forEach { metaData ->
//                    showToast(metaData.toString(), requireContext())
//
//                }
            } else {

                binding.uploadBtn.isEnabled = false
                metaDataAdapter.submitList(emptyList())
            }

        })

        viewModel.frontPagesLiveData.observe(viewLifecycleOwner) { coverPageMap ->
            hideLoadingOverlay()
            val fileNamesList = bookMetaDataList.map { metaData ->
                metaData.fileName!!
            }
            if (!coverPageMap.keys.containsAll(fileNamesList)) {
                showToast("Not all files were saved", requireContext())
            } else {
                showToast("All files were saved", requireContext())
                viewModel.createMutipleFileUploadWorkRequests(
                    bookMetaDataList,
                    selectedFolderOption?.folderId!!,
                    coverPageMap
                )
            }
            viewModel.resetState()
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG: String = "UploadBookFragment"
    }

    override fun onStart() {
        super.onStart()
        viewModel.attachFolderInfoEventListener()
    }

    override fun onPause() {
        super.onPause()
        viewModel.detachFolderInfoEventListener()
    }


}