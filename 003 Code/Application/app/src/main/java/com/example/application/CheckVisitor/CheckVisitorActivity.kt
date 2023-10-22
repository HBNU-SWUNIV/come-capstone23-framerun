package com.example.application.CheckVisitor

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.application.InfoDTO
import com.example.application.R



class CheckVisitorActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var adapter: RecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitor)

        recyclerView = findViewById(R.id.visitor_recyclerView)
        manager = GridLayoutManager(this, 3)

        recyclerView.layoutManager = manager


        // 인텐트에서 데이터를 받아옴
        val image = intent.getStringExtra("image")
        val title = intent.getStringExtra("title")

        // image 및 title이 null이 아닌 경우에만 InfoDTO를 초기화합니다.
        if (image != null && title != null) {
            var intruder = InfoDTO(image, title)
            var intruderList = listOf(intruder)

        adapter = RecyclerAdapter(intruderList)
        recyclerView.adapter = adapter

        } else {
            Log.e(TAG, "image 또는 title이 null입니다.")
        }







        /** DynamicLink 수신확인 */
        //initDynamicLink()
    }
}