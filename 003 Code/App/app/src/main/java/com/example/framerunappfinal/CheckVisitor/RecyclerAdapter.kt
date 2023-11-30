package com.example.framerunappfinal.CheckVisitor


import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.framerunappfinal.CheckVisitor.FullImageActivity
import com.example.framerunappfinal.InfoDTO
import com.example.framerunappfinal.databinding.ItemInfoBinding

import com.google.firebase.storage.FirebaseStorage

class RecyclerAdapter(private var itemList: MutableList<InfoDTO>, private val context: Context) : RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InfoDTO) {
            binding.info = item

            item.thumbnail?.let { thumbnail ->
                val storageRef = FirebaseStorage.getInstance().reference.child("Outsider_Img/$thumbnail")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(binding.itemInfoThumbnail.context)
                        .load(uri)
                        .into(binding.itemInfoThumbnail) // 'itemInfoThumbnail'로 수정

                    itemView.setOnClickListener {
                        val intent = Intent(context, FullImageActivity::class.java).apply {
                            putExtra("imageUrl", uri.toString())
                            putExtra("imageTitle", item.title)
                        }
                        Log.d("RecyclerAdapter", "FullImageActivity를 URL: ${uri} 와 제목: ${item.title}로 여는 중")
                        context.startActivity(intent)
                    }
                }.addOnFailureListener {
                    // 이미지 로드 실패 시 처리
                    Log.e("RecyclerAdapter", "Failed to load image: $thumbnail", it)
                    // 기본 이미지 설정 (예: 'res/drawable/default_image.png')
                    //binding.itemInfoThumbnail.setImageResource(R.drawable.default_image) // 'default_image'는 기본 이미지 리소스의 ID
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInfoBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    fun updateData(newItems: List<InfoDTO>) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }
}
