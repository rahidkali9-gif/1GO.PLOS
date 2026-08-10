package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadHelper {

    suspend fun saveBase64ImageToGallery(
        context: Context,
        base64Data: String,
        fileName: String,
        mimeType: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanData = if (base64Data.contains(",")) {
                base64Data.split(",")[1]
            } else {
                base64Data
            }
            val decodedBytes = Base64.decode(cleanData, Base64.DEFAULT)
            val inputStream = ByteArrayInputStream(decodedBytes)
            val uri = saveInputStreamToGallery(context, inputStream, fileName, mimeType)
            uri != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun downloadUrlToGallery(
        context: Context,
        urlStr: String,
        suggestedFileName: String,
        mimeType: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val finalFileName = if (suggestedFileName.isBlank()) {
                    "image_${System.currentTimeMillis()}.jpg"
                } else {
                    suggestedFileName
                }
                val uri = saveInputStreamToGallery(context, inputStream, finalFileName, mimeType)
                connection.disconnect()
                uri != null
            } else {
                connection.disconnect()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun saveInputStreamToGallery(
        context: Context,
        inputStream: InputStream,
        fileName: String,
        mimeType: String
    ): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(
                MediaStore.Images.Media.MIME_TYPE,
                if (mimeType.isBlank() || !mimeType.startsWith("image/")) "image/jpeg" else mimeType
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WebConnect")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        return try {
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(imageUri, null, null)
            null
        }
    }
}
