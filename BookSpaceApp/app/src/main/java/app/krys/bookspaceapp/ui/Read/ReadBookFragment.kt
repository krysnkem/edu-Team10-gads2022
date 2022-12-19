package app.krys.bookspaceapp.ui.Read

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import app.krys.bookspaceapp._util.showToast
import app.krys.bookspaceapp.data.model.BookInfo
import app.krys.bookspaceapp.databinding.FragmentReadBookBinding


/**
 * [ReadBookFragment] handles the book reading UI and functionality
 */
class ReadBookFragment : Fragment() {
   private var _binding: FragmentReadBookBinding? = null
   private val binding: FragmentReadBookBinding get() = _binding!!
    private var url: String? = null
    private lateinit var bookInfo: BookInfo
    val args: ReadBookFragmentArgs by navArgs()

    val viewModel: ReadBookViewModel by viewModels {
        ReadBookViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (args.booInfo.downloadUrl != null)
            url = args.booInfo.downloadUrl!!

        bookInfo = args.booInfo
        _binding = FragmentReadBookBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        url?.let { viewModel.loadBook(bookInfo) }
        showLoadingOverlay()
        viewModel.pdfBytes.observe(viewLifecycleOwner, Observer<ByteArray> { byteArray ->
            hideLoadingOverlay()
            if (byteArray == null){
                showToast("Error Loading File", requireContext())
                return@Observer
            }
            binding.pdfView.fromBytes(byteArray)
                .defaultPage(0)
                .swipeHorizontal(true)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .load()
        })

    }

    private fun hideLoadingOverlay() {
        binding.progressOverlay.visibility = View.INVISIBLE
    }

    private fun showLoadingOverlay() {
        binding.progressOverlay.visibility = View.VISIBLE
    }
}