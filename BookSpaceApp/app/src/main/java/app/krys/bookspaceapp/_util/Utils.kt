package app.krys.bookspaceapp._util

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

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

/**
 * Writes bitmap to a temporary file and returns the Uri for the file
 * @param applicationContext Application context
 * @param bitmap Bitmap to write to temp file
 * @return Uri for temp file with bitmap
 * @throws FileNotFoundException Throws if bitmap file cannot be found
 */
@Throws(FileNotFoundException::class)
suspend fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    val name = String.format("book-image-%s.png", UUID.randomUUID().toString())
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
    if (!outputDir.exists()) {
        outputDir.mkdirs() // should succeed
    }
    val outputFile = File(outputDir, name)
    var out: FileOutputStream? = null
    try {
        out = withContext(Dispatchers.IO) {
            FileOutputStream(outputFile)
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
    } finally {
        out?.let {
            try {
                it.close()
            } catch (ignore: IOException) {
            }

        }
    }
    return Uri.fromFile(outputFile)
}

/**
 * Writes pdf byte array to a temporary file and returns the Uri for the file
 * @param applicationContext Application context
 * @param byteArray ByteArray to write to temp file
 * @return ByteArray for temp file
 * @throws FileNotFoundException Throws if byteArray file cannot be found
 */
@Throws(FileNotFoundException::class)
suspend fun writeByteArrayToFile(applicationContext: Context, byteArray: ByteArray, name: String, folderId: String): ByteArray {
    return withContext(Dispatchers.IO){
        val outputDir = File(applicationContext.filesDir, folderId)
        if (!outputDir.exists()) {
            outputDir.mkdirs() // should succeed
        }
        val outputFile = File(outputDir, name)
        try {
            outputFile.writeBytes(byteArray)
            outputFile.readBytes()
        }catch (e: IOException){
            e.printStackTrace()
            throw  e
        }
    }

}

/**
 * Writes pdf byte array to a temporary file and returns the Uri for the file
 * @param applicationContext Application context
 * @param name String name to temp file
 * @return ByteArray for temp file
 * @throws FileNotFoundException Throws if byteArray file cannot be found
 */
@Throws(FileNotFoundException::class)
suspend fun loadBytesArrayFromFile(applicationContext: Context, name: String, folderId: String): ByteArray {
    return withContext(Dispatchers.IO){
        val outputDir = File(applicationContext.filesDir, folderId)
        if (!outputDir.exists()) {
            outputDir.mkdirs() // should succeed
        }
        val outputFile = File(outputDir, name)
        outputFile.readBytes()
    }

}

fun deleteCachedFiles(applicationContext: Context, folderId: String): Boolean {
    val outputDir = File(applicationContext.filesDir, folderId)
    if (!outputDir.exists()) {
        return false// should succeed
    }
    return outputDir.delete()
}





/**
 * checksIf a pdf file exists
 * @param applicationContext Application context
 * @param name String containing the filename
 * @return True if file exists and false if file or directory does not exist
 * @throws FileNotFoundException Throws if bitmap file cannot be found
 */
@Throws(FileNotFoundException::class)
suspend fun pdfFileExists(applicationContext: Context, name: String, folderId: String): Boolean {
    val outputDir = File(applicationContext.filesDir, folderId)
    if (!outputDir.exists()) {
        return false// should succeed
    }
    return File(outputDir, name).exists()

}


