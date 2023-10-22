package com.example.application.UserRegistration

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.application.R


class UserAdapter(val context: Context): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var list = mutableListOf<User>()

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var title = itemView.findViewById<TextView>(R.id.tvUserItem)
        var timestamp = itemView.findViewById<TextView>(R.id.tvTimeStamp)
        var content = itemView.findViewById<TextView>(R.id.tvUserItem2)


        fun onBind(data: User) {
            title.text = data.title
            content.text = data.content
            timestamp.text = data.timestamp


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun update(newList: MutableList<User>) {
        this.list = newList
        notifyDataSetChanged()
    }


}