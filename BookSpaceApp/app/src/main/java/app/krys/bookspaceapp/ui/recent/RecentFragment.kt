package app.krys.bookspaceapp.ui.recent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import app.krys.bookspaceapp.databinding.FragmentRecentBinding
import app.krys.bookspaceapp.layoutmanagers.GridLayoutManagerWrapper
import app.krys.bookspaceapp.ui.adapter.myspace.FolderContentAdapter
import app.krys.bookspaceapp.ui.adapter.myspace.OnSelectBookOptionListener
import app.krys.bookspaceapp.ui.home.HomeFragmentDirections


/**
 * [RecentFragment] shows recently viewed books
 */
class RecentFragment : Fragment() {
    private var _binding: FragmentRecentBinding? = null
    private val binding get() = _binding!!

    private lateinit var booksAdapter: FolderContentAdapter

    private val viewModel by viewModels<RecentBooksViewModel> {
        RecentBooksViewModelFactory(requireActivity().application)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoading()
        booksAdapter = FolderContentAdapter(viewModel.options,
            OnSelectBookOptionListener { bookInfo ->
                val action = HomeFragmentDirections.actionNavHomeToReadBookFragment(bookInfo)
                findNavController().navigate(action)
            }
        )
        binding.recentBooksRv.adapter = booksAdapter
        booksAdapter.dataChanged.observe(viewLifecycleOwner) {
            hideLoading()
        }
        booksAdapter.setCount()
        booksAdapter.itemCount.observe(viewLifecycleOwner, Observer<Int>{
            if (it < 1){
                showMessageText()
            } else {
                hideMessageText()
            }
        })


    }

    private fun hideMessageText() {
        binding.messageTv.visibility = View.GONE
    }

    private fun showMessageText() {
        binding.messageTv.visibility = View.VISIBLE
    }



    private fun showLoading() {
        binding.progressOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressOverlay.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        booksAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        booksAdapter.startListening()
    }

}