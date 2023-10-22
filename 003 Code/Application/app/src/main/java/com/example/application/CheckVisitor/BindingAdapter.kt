package com.example.application.CheckVisitor

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import androidx.databinding.BindingAdapter


object BindingAdapter {
    @JvmStatic
    @BindingAdapter("app:imageUrl")
    fun loadImage(imageView: ImageView, url: String){

        val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://framerun-cloud.appspot.com/")
        val storageReference = storage.reference
        val pathReference = storageReference.child("Outsider_Img/$url")

        pathReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(imageView.context)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .into(imageView)
        }
    }
}