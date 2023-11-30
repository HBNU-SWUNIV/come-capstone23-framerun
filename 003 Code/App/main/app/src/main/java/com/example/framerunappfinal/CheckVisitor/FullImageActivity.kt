package com.example.framerunappfinal.CheckVisitor

import android.content.Context
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.framerunappfinal.databinding.ActivityFullImageBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FullImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("imageUrl")
        val imageTitle = intent.getStringExtra("imageTitle") ?: "downloaded_image"

        Log.d(TAG, "imageUrl : $imageUrl imageTitle: $imageTitle")

        imageUrl?.let {
            Glide.with(this).load(it).into(binding.fullImageView)

            binding.downloadButton.setOnClickListener {
                downloadImage(imageUrl, imageTitle)
            }
        }?: run{
            Log.e("FullImageActivity", "Image URL is null")
        }
    }

    private fun downloadImage(imageUrl: String, fileName: String) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveImageToExternalStorage(resource, "$fileName.jpg")
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun saveImageToExternalStorage(bitmap: Bitmap, fileName: String) {
        // 안드로이드 Q(10) 이상인 경우 MediaStore API를 사용합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        // 미디어 스캐너에 이미지 파일을 알립니다.
                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                        Log.d("YourApp", "Image saved to gallery: $uri")
                    } ?: run {
                        Log.e("YourApp", "Failed to get output stream")
                    }
                } catch (e: IOException) {
                    Log.e("YourApp", "Error saving image", e)
                }
            } ?: run {
                Log.e("YourApp", "Failed to create media store entry")
            }
        } else {
            // 안드로이드 Q 미만의 버전에서는 다른 방식으로 파일 경로를 설정합니다.
            // 예: 외부 저장소의 Pictures 디렉토리에 저장
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val image = File(imagesDir, "$fileName.jpg")
            try {
                val outputStream = FileOutputStream(image)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                // 미디어 스캐너에 이미지 파일을 알립니다.
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)))
                Log.d("YourApp", "Image saved to gallery: ${image.absolutePath}")
            } catch (e: IOException) {
                Log.e("YourApp", "Error saving image", e)
            }
        }
    }
}
