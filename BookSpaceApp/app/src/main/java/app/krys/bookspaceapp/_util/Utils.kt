package app.krys.bookspaceapp._util

import android.app.AlertDialog
import android.content.Context
import android.view.View

fun createDialog(view: View, context: Context, action: ()->Unit): AlertDialog {
    val builder = AlertDialog.Builder(context)
    val alertDialog = builder.setView(view)
        .create()
//    alertDialog.setOnShowListener {
//        action()
//    }
    action()
    return alertDialog
}