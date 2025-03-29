package com.example.staysafe.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraService(private val context: Context) {
    private var currentPhotoPath: String? = null

    suspend fun capturePhoto(): Uri? {
        return try {
            // Create an image file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",        /* suffix */
                storageDir     /* directory */
            )

            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = image.absolutePath

            // Create the FileProvider URI
            FileProvider.getUriForFile(
                context,
                "com.example.staysafe.fileprovider",
                image
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCurrentPhotoPath(): String? = currentPhotoPath

    fun shareEmergencyPhoto(photoPath: String, contacts: List<String>) {
        val photoFile = File(photoPath)
        val photoUri = FileProvider.getUriForFile(
            context,
            "com.example.staysafe.fileprovider",
            photoFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, photoUri)
            putExtra(Intent.EXTRA_TEXT, "EMERGENCY: This is an emergency photo from StaySafe app.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Share with each contact
        contacts.forEach { contact ->
            val contactIntent = shareIntent.clone() as Intent
            contactIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(contact))
            context.startActivity(Intent.createChooser(contactIntent, "Share Emergency Photo"))
        }
    }
} 