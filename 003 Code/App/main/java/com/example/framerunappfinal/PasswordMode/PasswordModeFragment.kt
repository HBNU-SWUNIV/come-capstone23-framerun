package com.example.framerunappfinal.PasswordMode

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.framerunappfinal.databinding.FragmentPasswordModeBinding
import com.example.framerunappfinal.putPasswordMode


class PasswordModeFragment : Fragment() {
    private var _binding: FragmentPasswordModeBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPasswordModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            sharedPreferences = it.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        }

        // 최초 다운로드 시에만 mode2 스위치가 "On"으로 설정
        if (sharedPreferences.getBoolean("initialDownload", false).not()) {
            setupInitialDownload()
        }

        // 상태 복원
        val isMode1Checked = sharedPreferences.getBoolean("mode1Checked", false)
        val isMode2Checked = sharedPreferences.getBoolean("mode2Checked", false)
        val isMode3Checked = sharedPreferences.getBoolean("mode3Checked", false)

        binding.mode1Sw.isChecked = isMode1Checked
        binding.mode2Sw.isChecked = isMode2Checked
        binding.mode3Sw.isChecked = isMode3Checked

        binding.mode1Sw.setOnCheckedChangeListener { compoundButton, onSwitch ->
            if(binding.mode3Sw.isChecked){
                binding.mode1Sw.isChecked =false
            }else {
                saveSwitchState("mode1Checked", onSwitch)
                if (onSwitch) {
                    val mode = "mode_2"
                    putPasswordMode(mode)
                    binding.mode2Sw.isChecked = false
                    binding.mode3Sw.isChecked = false
                    Toast.makeText(requireContext(), "다중잠금 On", Toast.LENGTH_SHORT).show()

                } else {
                    binding.mode2Sw.isChecked = true
                    binding.mode3Sw.isChecked = false
                    Toast.makeText(requireContext(), "다중잠금 Off", Toast.LENGTH_SHORT).show()


                }
            }
        }

        binding.mode2Sw.setOnCheckedChangeListener { compoundButton, onSwitch ->
            saveSwitchState("mode2Checked", onSwitch)
            if(onSwitch){
                val mode = "mode_1"
                putPasswordMode(mode)
                binding.mode1Sw.isChecked = false
                binding.mode3Sw.isChecked = false
                Toast.makeText(requireContext(),"기본잠금 On", Toast.LENGTH_SHORT).show()

            }
            else{
                Toast.makeText(requireContext(),"기본잠금 Off", Toast.LENGTH_SHORT).show()


            }
        }

        binding.mode3Sw.setOnCheckedChangeListener { compoundButton, onSwitch ->
            if (binding.mode1Sw.isChecked) {
                binding.mode3Sw.isChecked = false
                Toast.makeText(requireContext(), "다중잠금이 활성화되어 있습니다.", Toast.LENGTH_SHORT).show()
            } else {
                saveSwitchState("mode3Checked", onSwitch)
                if (onSwitch) {
                    binding.mode1Sw.isChecked = false
                    binding.mode2Sw.isChecked = false
                    activity?.let {
                        val intent = Intent(it, RandomPassword::class.java)
                        it.startActivity(intent)
                    }
                    Toast.makeText(requireContext(), "임시비밀번호 On", Toast.LENGTH_SHORT).show()

                    handler.postDelayed({
                        if (binding.mode3Sw.isChecked) {
                            binding.mode2Sw.isChecked = true
                            binding.mode3Sw.isChecked = false
                            Toast.makeText(requireContext(), "임시비밀번호 종료", Toast.LENGTH_SHORT).show()
                        }
                    }, 6*100 * 1000)// 10분을 밀리초로 변환하여 지연 시간 설정
                } else {

                    binding.mode2Sw.isChecked = true
                    binding.mode1Sw.isChecked = false
                    Toast.makeText(requireContext(), "임시비밀번호 Off", Toast.LENGTH_SHORT).show()


                }
            }
        }
    }

    private fun setupInitialDownload() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("initialDownload", true) // 최초 다운로드 시 "On"으로 설정
        editor.apply()
    }
    private fun saveSwitchState(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}