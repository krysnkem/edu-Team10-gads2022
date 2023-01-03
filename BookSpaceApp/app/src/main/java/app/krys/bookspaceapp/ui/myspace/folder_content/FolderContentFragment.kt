package app.krys.bookspaceapp.ui.myspace.folder_content

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.krys.bookspaceapp._util.createDialog
import app.krys.bookspaceapp.data.model.BookInfo
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.databinding.BookOptionDialogLayoutBinding
import app.krys.bookspaceapp.databinding.FragmentFolderContentBinding
import app.krys.bookspaceapp.ui.adapter.myspace.FolderContentAdapter
import app.krys.bookspaceapp.ui.adapter.myspace.OnSelectBookOptionListener


class FolderContentFragment : Fragment() {

    private var _binding: FragmentFolderContentBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<FolderContentFragmentArgs>()
    private lateinit var folderInfo: FolderInfo
    private lateinit var bookAdapter: FolderContentAdapter
    private val viewModel by viewModels<FolderContentViewModel> {
        FolderContentViewModelFactory(requireActivity().application)
    }
    private lateinit var bookOptionBinding: BookOptionDialogLayoutBinding
    private lateinit var bookOptionDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        folderInfo = args.folderInfo
        bookOptionBinding = BookOptionDialogLayoutBinding.inflate(layoutInflater)
        _binding = FragmentFolderContentBinding.inflate(inflater, container, false)
        bookOptionDialog = createDialog(bookOptionBinding.root, requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoading()
        binding.folderNameTv.text = folderInfo.folderName
        bookAdapter = FolderContentAdapter(viewModel.getBookOptions(folderInfo.folderId!!),
            OnSelectBookOptionListener { bookInfo ->
                bookOptionDialog.show()
                setUpDialogMethods(bookInfo)
            }
       )
        bookAdapter.setCount()
        binding.folderContentRv.adapter = bookAdapter
        bookAdapter.dataChanged.observe(viewLifecycleOwner) {
            hideLoading()
        }
        bookAdapter.itemCount.observe(viewLifecycleOwner){count->
            if (count  < 1){
                showMessageText()
            }else{
                hideMessageText()
            }

        }
    }

    private fun hideMessageText() {
        binding.messageTv.visibility = View.GONE
    }

    private fun showMessageText() {
        binding.messageTv.visibility = View.VISIBLE
    }

    private fun setUpDialogMethods(bookInfo: BookInfo) {
        bookOptionBinding.readOption.setOnClickListener {
            bookOptionDialog.dismiss()
            val action =
                FolderContentFragmentDirections.actionFolderContentFragmentToReadBookFragment(
                    bookInfo
                )
            findNavController().navigate(action)
        }
        bookOptionBinding.removeOption.setOnClickListener {
            viewModel.removeBook(bookInfo)
            bookOptionDialog.dismiss()
            bookAdapter.reduceCount()
        }

    }

    private fun showLoading() {
        binding.progressOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressOverlay.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        bookAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        bookAdapter.stopListening()
    }

}