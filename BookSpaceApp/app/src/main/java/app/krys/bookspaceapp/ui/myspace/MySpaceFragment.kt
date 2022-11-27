package app.krys.bookspaceapp.ui.myspace

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import app.krys.bookspaceapp.databinding.FragmentCreateFolderBinding
import app.krys.bookspaceapp.databinding.FragmentMySpaceBinding
import app.krys.bookspaceapp.ui.home.HomeFragmentDirections
import app.krys.bookspaceapp._util.createDialog

/**
 * [MySpaceFragment] shows the users library space
 */
class MySpaceFragment : Fragment() {

    private var _binding: FragmentMySpaceBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMySpaceBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialogLayout = FragmentCreateFolderBinding.inflate(layoutInflater)


        binding.addFolderFab.setOnClickListener {
//               CreateFolderFragment().show(childFragmentManager, CreateFolderFragment.TAG)
            dialog = createDialog(
                dialogLayout.root,
                requireContext()
            ) {
                dialogLayout.createBtn.setOnClickListener {
                    dialog.dismiss()
                }
            }
            dialog.show()
        }

        binding.uploadFab.setOnClickListener {
            val action = HomeFragmentDirections.actionNavHomeToUploadBookFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}