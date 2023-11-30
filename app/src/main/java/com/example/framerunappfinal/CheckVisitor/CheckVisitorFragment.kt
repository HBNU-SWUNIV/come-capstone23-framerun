package com.example.framerunappfinal.CheckVisitor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.framerunappfinal.InfoDTO
import com.example.framerunappfinal.R

class CheckVisitorFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapter
    private lateinit var viewModel: CheckVisitorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_check_visitor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CheckVisitorViewModel::class.java)

        recyclerView = view.findViewById(R.id.visitor_recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = RecyclerAdapter(mutableListOf(), requireContext())
        recyclerView.adapter = adapter

        viewModel.intruders.observe(viewLifecycleOwner, { intruders ->
            adapter.updateData(intruders.map { InfoDTO(it.image, it.title) })
        })
    }

}