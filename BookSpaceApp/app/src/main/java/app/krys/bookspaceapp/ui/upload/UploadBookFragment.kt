package app.krys.bookspaceapp.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import app.krys.bookspaceapp.databinding.FragmentUploadBookBinding
import com.google.android.material.snackbar.Snackbar


/**
 * [UploadBookFragment] handles the books upload screen and functionality
 */
class UploadBookFragment : Fragment() {
    private var _binding: FragmentUploadBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterItemListener: AdapterView.OnItemClickListener


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

        val folders = listOf(
            "Lecture Materials",
            "Novels",
            "Science",
            "Business books"
        )

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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}