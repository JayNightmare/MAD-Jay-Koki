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

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun getCameraIntent(): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        val photoURI: Uri = FileProvider.getUriForFile(
            context,
            "com.example.staysafe.fileprovider",
            photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        return intent
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