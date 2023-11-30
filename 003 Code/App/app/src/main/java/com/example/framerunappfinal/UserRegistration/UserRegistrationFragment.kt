package com.example.framerunappfinal.UserRegistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.framerunappfinal.databinding.FragmentUserRegistration2Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRegistrationFragment : Fragment() {
    private var _binding: FragmentUserRegistration2Binding? = null
    private val binding get() = _binding!!


    private lateinit var userViewModel: UserViewModel
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserRegistration2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        UserRepository.initialize(requireActivity().applicationContext) // UserRepository 초기화 추가

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)


        //userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        userViewModel.userList.observe(viewLifecycleOwner) {
            userAdapter.update(it)
        }

        userAdapter = UserAdapter(requireContext())
        binding.rvUserList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserList.adapter = userAdapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireActivity(), EditUserActivity::class.java).apply {
                putExtra("type", "ADD")
            }
            requestActivity.launch(intent)
        }
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val user = it.data?.getSerializableExtra("user") as User

            when(it.data?.getIntExtra("flag", -1)) {
                0 -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        userViewModel.insert(user)
                    }
                    Toast.makeText(requireActivity(), "추가되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}