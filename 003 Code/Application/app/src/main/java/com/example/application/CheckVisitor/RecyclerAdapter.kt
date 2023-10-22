package com.example.application.CheckVisitor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.application.InfoDTO
import com.example.application.databinding.ItemInfoBinding



class RecyclerAdapter(private val itemList: List<InfoDTO>): RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>(){

    inner class MyViewHolder(val binding: ItemInfoBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InfoDTO) {
            binding.info = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val listItemBinding = ItemInfoBinding.inflate(inflater, parent, false)
        return MyViewHolder(listItemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}