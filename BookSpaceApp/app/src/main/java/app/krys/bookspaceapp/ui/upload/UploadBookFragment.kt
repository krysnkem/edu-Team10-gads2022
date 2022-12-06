package app.krys.bookspaceapp.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.databinding.FragmentUploadBookBinding
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.pdf417.PDF417ResultMetadata


/**
 * [UploadBookFragment] handles the books upload screen and functionality
 */
class UploadBookFragment : Fragment() {
    private var _binding: FragmentUploadBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterItemListener: AdapterView.OnItemClickListener

    private val viewModel by activityViewModels<UploadBookViewModel> {
        UploadBookViewModelFactory()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUploadBookBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val folders = mutableListOf<String>()
        val folderInfoList = mutableListOf<FolderInfo>()

        adapterItemListener = AdapterView.OnItemClickListener { p0, p1, position, id ->
            Snackbar.make(view, "${folders.get(position)} selected", Snackbar.LENGTH_LONG).show()
        }
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            folders
        )
            .apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        binding.spinner.adapter = arrayAdapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
                Toast.makeText(
                    requireContext(), "Folder String: ${folders[position]}\n" +
                            "Folder Info: ${folderInfoList[position].folderName}", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}