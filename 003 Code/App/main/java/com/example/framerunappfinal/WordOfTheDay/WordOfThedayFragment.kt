package com.example.framerunappfinal.WordOfTheDay

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.framerunappfinal.CountUpTextView
import com.example.framerunappfinal.R
import com.example.framerunappfinal.RecordButton
import com.example.framerunappfinal.State
import com.example.framerunappfinal.databinding.FragmentWordOfThedayBinding
import com.example.framerunappfinal.uploadAudioFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WordOfThedayFragment : Fragment() {
    private var _binding: FragmentWordOfThedayBinding? = null
    private val binding get() = _binding!!

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false
    private var isPlaying = false
    private var recordingFile: File? = null

    private val samplingRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var bufferSize: Int = 0

    // WAV 파일 헤더를 위한 정보
    private val headerSize = 44
    private val channels = 1

    private lateinit var randomTextView : TextView
    private lateinit var recordTimeTextView: CountUpTextView
    private lateinit var resetButton: Button
    private lateinit var RecordButton: RecordButton
    private lateinit var confirmButton: Button


    private var state = State.BEFORE_RECORDING
        set(value) {
            field = value
            resetButton.isEnabled = value == State.AFTER_RECORDING || value == State.ON_RECORDING
            RecordButton.updateIconWithState(value)
        }


    private lateinit var prefs: SharedPreferences // SharedPreferences 인스턴스를 클래스 변수로 선언


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWordOfThedayBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        randomTextView = binding.randomTextView
        recordTimeTextView = binding.recordTimeTextView
        resetButton = binding.resetButton
        RecordButton = binding.recordButton
        confirmButton = binding.confirmButton



        prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE) // SharedPreferences 초기화
        val isFirstRun = prefs.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            val initialRandomString = generateRandomString(requireContext())
            saveRandomString(initialRandomString)
            prefs.edit().putBoolean("isFirstRun", false).apply()
        }


        randomTextView.text = loadRandomString() ?: "포근하게 피어나는 해바라기 순식간에 넘치는 커피잔"

        // 랜덤 문자열 생성 및 매일 자정 스케줄 설정
        AlarmSetup.scheduleRandomStringGeneration(requireContext())
        // UI 및 변수 초기화

        initViews()
        bindViews()
        initVariables()
    }



    private fun saveRandomString(randomString: String) {
        // 클래스 변수 prefs를 사용
        prefs.edit().putString("lastRandomString", randomString).apply()
    }

    private fun loadRandomString(): String? {
        // 클래스 변수 prefs를 사용
        return prefs.getString("lastRandomString", null)
    }

    private fun generateRandomString(context: Context): String {
        val adjectiveVerbPhrases = context.resources.getStringArray(R.array.adjective_verb_phrases)
        val nouns = context.resources.getStringArray(R.array.nouns)

        val randomPhrase1 = adjectiveVerbPhrases.random()
        val randomNoun1 = nouns.random()
        val randomPhrase2 = adjectiveVerbPhrases.random()
        val randomNoun2 = nouns.random()

        return "$randomPhrase1 $randomNoun1 $randomPhrase2 $randomNoun2"
    }


    private fun initViews() {
        RecordButton.updateIconWithState(state)

//        // 확인 버튼 초기 비활성화
//        confirmButton.isEnabled = false
    }

    private fun initVariables() {
        state = State.BEFORE_RECORDING
        confirmButton.isEnabled = false
        bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat)
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = 44100 * 2 // 대체 버퍼 크기 설정
        }


    }

    private fun bindViews() {
        RecordButton.setOnClickListener {
            when (state) {
                State.BEFORE_RECORDING -> {
                    if (checkPermission()) {
                        startRecording()
                    }
                }
                State.ON_RECORDING -> stopRecording()
                State.AFTER_RECORDING -> startPlaying()
                State.ON_PLAYING -> stopPlaying()
            }
        }

        resetButton.setOnClickListener {
            resetRecording()
        }

        confirmButton.setOnClickListener {
            // 확인 버튼에 대한 처리
            confirmButton.isEnabled = false
            state = State.BEFORE_RECORDING
            recordTimeTextView.clearCountTime()


            //화자 인식 -> 파이어베이스 스토리지에 올리면 오래걸림
            //시간 단축을 위해 서버에 오디오 파일 전송
            recordingFile?.let { file ->
                uploadAudioFile(requireContext(),file)
                val filepath = file.absolutePath
                Log.d(ContentValues.TAG, "file : $file")
                Log.d(ContentValues.TAG, "filePath : $filepath")

            } ?: run {
                // recordingFile이 null인 경우의 처리
                Toast.makeText(requireActivity(), "녹음 파일이 없습니다.", Toast.LENGTH_SHORT).show()
            }


        }
    }

//    private fun checkTranscriptionMatch() {
//        val transcription = transcriptionTextView.text.toString()
//        val randomString = randomTextView.text.toString()
//
////        confirmButton.isEnabled = transcription == randomString
//        // transcriptionTextView 텍스트와 randomTextView 텍스트가 같으면 confirmButton 활성화
//        val isMatch = transcription == randomString
//        confirmButton.isEnabled = isMatch
//
//        // 버튼이 활성화되었을 때 토스트 메시지 표시
//        if (isMatch) {
//            Toast.makeText(this,"활성화됨",Toast.LENGTH_SHORT).show()
//        }
//    }



    // onReceiveRandomString 함수는 BroadcastReceiver에서 호출
    fun onReceiveRandomString(randomString: String) {
        activity?.runOnUiThread {
            randomTextView.text = randomString
        }
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return false
        }
        return true
    }


    private fun startRecording() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val rawFileName = "recording_${dateFormat.format(Date())}.pcm"
            val wavFileName = "recording_${dateFormat.format(Date())}.wav"
            val rawFile = File(requireActivity().filesDir, rawFileName)
            val wavFile = File(requireActivity().filesDir, wavFileName)
            recordingFile = wavFile

            val outputStream = FileOutputStream(rawFile)
            val buffer = ByteArray(bufferSize)

            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
                return@launch
            }

            audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO, audioFormat, bufferSize)
            isRecording = true

            withContext(Dispatchers.Main) {
                state = State.ON_RECORDING
                RecordButton.updateIconWithState(state)
                recordTimeTextView.startCountUp()
            }

            audioRecord?.apply {
                startRecording()
                while (isRecording) {
                    val read = read(buffer, 0, bufferSize)
                    if (read > 0) {
                        outputStream.write(buffer, 0, read)
                    }
                }
                stop()
                release()
            }
            outputStream.close()

            if (rawFile.exists() && rawFile.length() > 0) {
                val conversionSuccess = convertPcmToWav(rawFile, wavFile, bufferSize,samplingRate, channels)
                rawFile.delete()
                // 변환된 WAV 파일을 사용하여 음성 인식 실행
                if (conversionSuccess && wavFile.exists() && wavFile.length() > 0) {
//                    val transcript = transcribeAudio(wavFile.absolutePath)
//                    Log.d("MainActivity", "Transcription result: $transcript")
//                    withContext(Dispatchers.Main) {
//                        transcriptionTextView.text = transcript
//                        checkTranscriptionMatch()
//                    }
                    withContext(Dispatchers.Main) {
                        confirmButton.isEnabled = true
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireActivity(), "WAV 파일 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.w("MainActivity", "PCM file does not exist or is empty: ${rawFile.absolutePath}")
            }
            Log.d("MainActivity", "Recording started successfully")

        } catch (e: Exception) {
            Log.e("MainActivity", "Error during recording: ${e.message}", e)
        }
    }

        private suspend fun convertPcmToWav(pcmFile: File, wavFile: File, bufferSize: Int, sampleRate: Int, channels: Int) : Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dataSize = pcmFile.length().toInt()
                val totalSize = dataSize + headerSize
                val byteRate = sampleRate * channels * (audioFormat / 8)

                val wavBuffer = ByteBuffer.allocate(headerSize + dataSize)
                wavBuffer.order(ByteOrder.LITTLE_ENDIAN)

                // WAV 파일 헤더 작성
                wavBuffer.put("RIFF".toByteArray(Charsets.US_ASCII)) // ChunkID
                wavBuffer.putInt(totalSize - 8) // ChunkSize
                wavBuffer.put("WAVE".toByteArray(Charsets.US_ASCII)) // Format
                wavBuffer.put("fmt ".toByteArray(Charsets.US_ASCII)) // Subchunk1ID
                wavBuffer.putInt(16) // Subchunk1Size
                wavBuffer.putShort(1) // AudioFormat (PCM)
                wavBuffer.putShort(1.toShort()) // NumChannels (모노)
                wavBuffer.putInt(sampleRate) // SampleRate
                wavBuffer.putInt(sampleRate * 2) // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
                wavBuffer.putShort(2.toShort()) // BlockAlign (NumChannels * BitsPerSample/8)
                wavBuffer.putShort(16.toShort()) // BitsPerSample (16비트)

                wavBuffer.put("data".toByteArray(Charsets.US_ASCII)) // Subchunk2ID
                wavBuffer.putInt(dataSize) // Subchunk2Size

                // PCM 데이터 추가
                val inputStream = FileInputStream(pcmFile).channel
                inputStream.read(wavBuffer)
                inputStream.close()

                wavBuffer.flip()

                // WAV 파일에 데이터 쓰기
                val fileChannel = FileOutputStream(wavFile).channel
                fileChannel.write(wavBuffer)
                fileChannel.close()
                Log.d("MainActivity", "PCM to WAV conversion successful")
                true

            } catch (e: Exception) {
                Log.e("MainActivity", "Error converting PCM to WAV: ${e.message}", e)
                false
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startRecording()
                } else {
                    // 권한이 거부되었을 때 처리
                }
            }
        }
    }
//    private fun transcribeAudio(audioFilePath: String): String {
//        val transcript = StringBuilder()
//
//        try {
//            // 자산에서 Google 서비스 계정 키 파일 불러오기
//            applicationContext.assets.open("framerun-cloud-62469e79569e.json").use { inputStream ->
//                val credentials = GoogleCredentials.fromStream(inputStream)
//                val speechSettings = SpeechSettings.newBuilder()
//                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
//                    .build()
//
//                // Speech-to-Text 클라이언트 생성 및 사용
//                SpeechClient.create(speechSettings).use { speechClient ->
//                    val audioBytes = Files.readAllBytes(Paths.get(audioFilePath))
//                    val audio = RecognitionAudio.newBuilder()
//                        .setContent(ByteString.copyFrom(audioBytes))
//                        .build()
//
//                    val config = RecognitionConfig.newBuilder()
//                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
//                        .setSampleRateHertz(44100)
//                        .setLanguageCode("ko-KR")
//                        .build()
//
//                    val response = speechClient.recognize(config, audio)
//
//                    for (result in response.resultsList) {
//                        transcript.append(result.alternativesList[0].transcript)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // 오류 처리
//        }
//
//        return transcript.toString()
//    }


    private fun stopRecording() = CoroutineScope(Dispatchers.IO).launch {
        isRecording = false
        withContext(Dispatchers.Main) {
            state = State.AFTER_RECORDING
            RecordButton.updateIconWithState(state)
            recordTimeTextView.stopCountUp()
        }

    }

    private fun startPlaying() = CoroutineScope(Dispatchers.IO).launch {
        recordingFile?.let { file ->
            if (file.exists()) {
                isPlaying = true
                withContext(Dispatchers.Main) {
                    state = State.ON_PLAYING
                    RecordButton.updateIconWithState(state)
                    recordTimeTextView.startCountUp()
                }

                val inputStream = FileInputStream(file)
                val buffer = ByteArray(bufferSize)
                audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, samplingRate, AudioFormat.CHANNEL_OUT_MONO, audioFormat, bufferSize, AudioTrack.MODE_STREAM)


                audioTrack?.play()
                var read: Int
                while (isPlaying) {
                    read = inputStream.read(buffer)
                    if (read == -1) break
                    audioTrack?.write(buffer, 0, read)
                }

                audioTrack?.stop()
                audioTrack?.release()
                inputStream.close()

                withContext(Dispatchers.Main) {
                    if (isPlaying) {
                        stopPlaying()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireActivity(), "녹음된 파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireActivity(), "녹음 파일이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopPlaying() = CoroutineScope(Dispatchers.IO).launch {
        isPlaying = false
        withContext(Dispatchers.Main) {
            state = State.AFTER_RECORDING
            RecordButton.updateIconWithState(state)
            recordTimeTextView.stopCountUp()
        }
    }

    private fun resetRecording() = CoroutineScope(Dispatchers.IO).launch {
        stopPlaying()
        recordingFile?.delete()
        recordingFile = null
        withContext(Dispatchers.Main) {
            recordTimeTextView.clearCountTime()
            state = State.BEFORE_RECORDING
            // transcriptionTextView 텍스트 초기화
//            transcriptionTextView.text = ""

            // 확인 버튼 비활성화
            confirmButton.isEnabled = false
        }
    }

//    private fun uploadDataToFirebase() {
//        // Firebase Storage 초기화
//        val storage = FirebaseStorage.getInstance()
//        val storageRef = storage.reference
//        val title = ""
//
//        // 업로드된 파일의 다운로드 URL 저장하기 위한 배열
//        val fileUrls = ArrayList<String>()
//        // 업로드 작업을 저장하기 위한 작업 목록
//
//        // 오디오 파일 업로드
//        if (!recordedAudioPath.isNullOrEmpty()) {
//            val audioUri = Uri.fromFile(File(recordedAudioPath))
//            val audioRef = storageRef.child("${user.title}/${audioUri.lastPathSegment}")
//
//
//            // 오디오 파일의 메타데이터 설정
//            val metadata = StorageMetadata.Builder()
//                .setContentType("audio/wav")
//                .build()
//
//            audioRef.putFile(audioUri, metadata).continueWithTask { task ->
//                if (!task.isSuccessful) {
//                    task.exception?.let { throw it }
//                }
//                audioRef.downloadUrl
//            }.addOnSuccessListener { uri ->
//                fileUrls.add(uri.toString())
////                uploadImages(user, imageList, fileUrls)
//            }.addOnFailureListener { exception ->
//                Log.e(TAG, "Audio upload failed", exception)
//            }
//        }
//
//        Log.d(TAG, "All files uploaded: $fileUrls")
//
//
//    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 202
    }


}