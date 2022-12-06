package app.krys.bookspaceapp._util

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext

fun createDialog(view: View, context: Context): AlertDialog {
    return  AlertDialog.Builder(context).setView(view)
        .create()
}

fun showToast(text: String, context: Context) {
    Toast.makeText(
        context,
        text,
        Toast.LENGTH_SHORT
    ).show()
}