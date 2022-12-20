package app.krys.bookspaceapp.data.pdf

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import app.krys.bookspaceapp.data.model.BookMetaData
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


class ReadPdfMetadata internal constructor(val data: Uri, val context: Context) {

    private var core: PdfiumCore = PdfiumCore(context)

    @Throws(IOException::class)
    suspend fun getPdfMetadata(): BookMetaData {

        return withContext(Dispatchers.IO) {
            val document = core.newDocument(context.contentResolver.openFileDescriptor(data, "r"))

            val md = core.getDocumentMeta(document)
            core.closeDocument(document)

//          mutableListOf(
//            md.producer + " ",
//            md.title + " ",
//            md.keywords + " ",
//            md.author + " ",
//            md.subject + " ",
//            md.creationDate?.toString() + " ",
//            md.modDate?.toString() + " "
//        )
            BookMetaData(
                author = md.author,
                title = md.title,
                size = getFileSize(),
                fileName = getFileName(),
                frontPage = getFirstPageBitmap()
            )
        }

    }

   private fun getFileName(): String? {
        var result: String? = null
        if (data.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(data, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst() && cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) >= 0) {
                    result = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        .let { cursor.getString(it) }
                }
            }
        }
        if (result == null) {
            result = data.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result
    }

   private fun getFileSize(): Long? {
        var result: Long? = null
        if (data.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(data, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst() && cursor.getColumnIndex(OpenableColumns.SIZE) >= 0) {
                    result = cursor.getColumnIndex(OpenableColumns.SIZE)
                        .let { cursor.getLong(it) }
                }
            } finally {
                cursor?.close()
            }
        }
        return result
    }

    private suspend fun getFirstPageBitmap(): Bitmap? {
        val pageNum = 0
        try {
            val pdfDocument: PdfDocument =
                core.newDocument(context.contentResolver.openFileDescriptor(data, "r"))
            core.openPage(pdfDocument, pageNum)
            val width = core.getPageWidthPoint(pdfDocument, pageNum)
            val height = core.getPageHeightPoint(pdfDocument, pageNum)


            // ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
            // RGB_565 - little worse quality, twice less memory usage
            val bitmap = Bitmap.createBitmap(
                width, height,
                Bitmap.Config.RGB_565
            )
            core.renderPageBitmap(
                pdfDocument, bitmap, pageNum, 0, 0,
                width, height
            )
            //if you need to render annotations and form fields, you can use
            //the same method above adding 'true' as last param
            core.closeDocument(pdfDocument) // important!
            return bitmap
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return null
    }


}