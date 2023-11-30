package com.example.framerunappfinal.CheckVisitor

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import androidx.databinding.BindingAdapter


object BindingAdapter {
    @JvmStatic
    @BindingAdapter("app:imageUrl")
    fun loadImage(imageView: ImageView, url: String?) {
        url?.let {
            val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://framerun-cloud.appspot.com/")
            val storageReference = storage.reference
            val pathReference = storageReference.child("Outsider_Img/$it")

            pathReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(imageView.context)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .into(imageView)
            }
            Log.d(TAG, "이미지 받기 성공")
        }
    }
}