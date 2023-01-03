package app.krys.bookspaceapp.ui.account.settings

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import app.krys.bookspaceapp.R
import app.krys.bookspaceapp.databinding.FragmentLogoutDialogBinding
import com.firebase.ui.auth.AuthUI


class LogoutDialogFragment : DialogFragment(), View.OnClickListener {

    private val TAG = this::class.simpleName

    private var _binding: FragmentLogoutDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLogoutDialogBinding.inflate(inflater, container, false)

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initialButtonForClickAction()
    }



    /** ----------------------- DIALOG SETUP --------------------- */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.logout_button -> signOut()
            R.id.cancel_button -> closeFragment()
        }
    }


    private fun initialButtonForClickAction() {
        binding.cancelButton.setOnClickListener(this)
        binding.logoutButton.setOnClickListener(this)
    }


    private fun closeFragment() {
        dialog!!.dismiss()
    }


    private fun signOut() {
        AuthUI.getInstance().signOut(requireContext())
            .addOnCompleteListener {

            }.addOnFailureListener {
                Log.d(TAG, "Unable to sign you out ${it.message}.")
            }

        // Firebase.auth.signOut()
    }


    override fun onResume() {
        super.onResume()

        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event?.action != KeyEvent.ACTION_DOWN) { // filter
                    closeFragment()
                    true
                } else {
                    // Hide your keyboard here
                    true
                }

            } else {
                // Pass on to be processed as normal
                false
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}