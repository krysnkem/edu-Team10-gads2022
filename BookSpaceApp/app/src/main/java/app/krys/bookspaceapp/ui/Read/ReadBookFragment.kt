package app.krys.bookspaceapp.ui.Read

import android.content.res.Configuration
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
    private val args: ReadBookFragmentArgs by navArgs()
    var bookHasLoaded: Boolean = false

    val viewModel: ReadBookViewModel by viewModels {
        ReadBookViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (args.booInfo.downloadUrl != null) {
            url = args.booInfo.downloadUrl!!
        }
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
            if (byteArray == null) {
                showToast("Error Loading File", requireContext())
                return@Observer
            }

            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    binding.progressOverlay.alpha = 0.1f
                } // Night mode is not active, we're using the light theme
                Configuration.UI_MODE_NIGHT_YES -> {
                    binding.progressOverlay.alpha = 0.0f
                } // Night mode is active, we're using dark theme
            }

            binding.bookNameTv.text = bookInfo.bookName
            binding.pdfView.fromBytes(byteArray)
                .defaultPage(bookInfo.currentPage?.toInt() ?: 0)
                .swipeHorizontal(true)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .onPageChange { page, pageCount ->
                    //using string format here because of warnings
                    binding.pageCountTv.text = String.format("${page + 1} / $pageCount pages")
                    bookInfo.currentPage = page.toLong()
                    bookInfo.lastRead = -1 * System.currentTimeMillis()
                    binding.pageSlider.value = page.toFloat() + 1
                    if (page == 0) {
                        binding.prevPageBtn.visibility = View.GONE
                    } else {
                        binding.prevPageBtn.visibility = View.VISIBLE
                    }
                    if (page + 1 == pageCount) {
                        binding.nextPageBtn.visibility = View.GONE
                    } else {
                        binding.nextPageBtn.visibility = View.VISIBLE
                    }

                }
                .onLoad {
                    bookHasLoaded = true
                    binding.pageSlider.apply {
                        if (binding.pdfView.pageCount == 1) {
                            visibility = View.GONE
                        } else {
                            visibility = View.VISIBLE
                            valueTo = binding.pdfView.pageCount.toFloat()
                            value = binding.pdfView.currentPage.toFloat()
                        }
                        if (bookInfo.currentPage == 0L) {
                            binding.prevPageBtn.visibility = View.GONE
                        }
                        if ((bookInfo.currentPage?.toInt()?.plus(1))== binding.pdfView.pageCount) {
                            binding.nextPageBtn.visibility = View.GONE
                        }
                        when (currentNightMode) {
                            Configuration.UI_MODE_NIGHT_NO -> {
                                binding.pdfView.setNightMode(false)
                            } // Night mode is not active, we're using the light theme
                            Configuration.UI_MODE_NIGHT_YES -> {
                                binding.pdfView.setNightMode(true)
                            } // Night mode is active, we're using dark theme
                        }
                        binding.pdfView.visibility = View.VISIBLE

                    }
                }
                .load()
        })
        binding.pageSlider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                binding.pdfView.jumpTo(value.toInt() - 1)
            }
        }
        binding.prevPageBtn.setOnClickListener {
            binding.pdfView.jumpTo(bookInfo.currentPage?.toInt()?.minus(1) ?: 0)
        }
        binding.nextPageBtn.setOnClickListener {
            binding.pdfView.jumpTo(bookInfo.currentPage?.toInt()?.plus(1) ?: 0)
        }



    }

    override fun onPause() {
        super.onPause()
        if (bookHasLoaded) {
            viewModel.addBookToRecent(bookInfo)
        }
    }

    private fun hideLoadingOverlay() {
        binding.progressOverlay.visibility = View.INVISIBLE
    }

    private fun showLoadingOverlay() {
        binding.progressOverlay.visibility = View.VISIBLE
    }

    companion object {
        const val TAG = "ReadBook"
    }
}