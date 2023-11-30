package com.example.framerunappfinal.UserRegistration

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.framerunappfinal.CountUpTextView
import com.example.framerunappfinal.R
import com.example.framerunappfinal.RecordButton
import com.example.framerunappfinal.State


import kotlinx.coroutines.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class AudioRecording : AppCompatActivity() {
    private var uraudioRecord: AudioRecord? = null
    private var uraudioTrack: AudioTrack? = null
    private var urisRecording = false
    private var urisPlaying = false
    private var urrecordingFile: File? = null

    private val ursamplingRate = 44100
    private val urchannelConfig = AudioFormat.CHANNEL_IN_MONO
    private val uraudioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var urbufferSize: Int = 0

    // WAV 파일 헤더를 위한 정보
    private val urheaderSize = 44
    private val urchannels = 1

    private val urrecordTimeTextView: CountUpTextView by lazy { findViewById(R.id.urrecordTimeTextView) }
    private val urresetButton: Button by lazy { findViewById(R.id.urresetButton) }
    private val urrecordButton: RecordButton by lazy { findViewById(R.id.urrecordButton) }
    private val urconfirmButton: Button by lazy { findViewById(R.id.urconfirmButton) }

    private var urstate = State.BEFORE_RECORDING
        set(value) {
            field = value
            urresetButton.isEnabled = value == State.AFTER_RECORDING || value == State.ON_RECORDING
            urrecordButton.updateIconWithState(value)
            //confirmButton.isEnabled = value == State.AFTER_RECORDING
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_recording)

        urinitViews()
        urbindViews()
        urinitVariables()
    }

    private fun urinitViews() {
        urrecordButton.updateIconWithState(urstate)
    }

    private fun urinitVariables() {
        urstate = State.BEFORE_RECORDING
        urconfirmButton.isEnabled = false
        urbufferSize = AudioRecord.getMinBufferSize(ursamplingRate, urchannelConfig, uraudioFormat)
        if (urbufferSize == AudioRecord.ERROR || urbufferSize == AudioRecord.ERROR_BAD_VALUE) {
            urbufferSize = 44100 * 2 // 대체 버퍼 크기 설정
        }
    }

    private fun urbindViews() {
        urrecordButton.setOnClickListener {
            when (urstate) {
                State.BEFORE_RECORDING -> {
                    if (checkPermission()) {
                        urstartRecording()
                    }
                }
                State.ON_RECORDING -> urstopRecording()
                State.AFTER_RECORDING -> urstartPlaying()
                State.ON_PLAYING -> urstopPlaying()
            }
        }

        urresetButton.setOnClickListener {
            urresetRecording()
        }

        urconfirmButton.setOnClickListener {
            urrecordingFile?.let { file ->
                // FileProvider 대신 파일의 경로를 직접 전달
                val intent = Intent().apply {
                    // 파일의 절대 경로를 문자열로 전달
                    putExtra("recordedAudioPath", file.absolutePath)
                }

                setResult(Activity.RESULT_OK, intent)
                urconfirmButton.isEnabled =false
                finish() // 액티비티 종료
            }
        }


    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this@AudioRecording, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@AudioRecording, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return false
        }
        return true
    }




    private fun urstartRecording() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val urdateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val urrawFileName = "recording_${urdateFormat.format(Date())}.pcm"
            val urwavFileName = "recording_${urdateFormat.format(Date())}.wav"
            val urrawFile = File(filesDir, urrawFileName)
            val urwavFile = File(filesDir, urwavFileName)
            urrecordingFile = urwavFile

            val uroutputStream = FileOutputStream(urrawFile)
            val urbuffer = ByteArray(urbufferSize)

            if (ActivityCompat.checkSelfPermission(
                    this@AudioRecording,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@AudioRecording,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
                return@launch
            }

            uraudioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                ursamplingRate,
                AudioFormat.CHANNEL_IN_MONO,
                uraudioFormat,
                urbufferSize
            )
            urisRecording = true

            withContext(Dispatchers.Main) {
                urstate = State.ON_RECORDING
                urrecordButton.updateIconWithState(urstate)
                urrecordTimeTextView.startCountUp()
            }

            uraudioRecord?.apply {
                startRecording()
                while (urisRecording) {
                    val read = read(urbuffer, 0, urbufferSize)
                    if (read > 0) {
                        uroutputStream.write(urbuffer, 0, read)
                    }
                }
                stop()
                release()
            }
            uroutputStream.close()


            if (urrawFile.exists() && urrawFile.length() > 0) {
                val conversionSuccess = urconvertPcmToWav(urrawFile, urwavFile, urbufferSize,ursamplingRate ,urchannels)
                urrawFile.delete()
                if(conversionSuccess && urwavFile.exists() && urwavFile.length() > 0){
                    withContext(Dispatchers.Main) {
                       urconfirmButton.isEnabled = true
                    }
                }
            } else {
                Log.w("MainActivity", "PCM file does not exist or is empty: ${urrawFile.absolutePath}")
            }
            Log.d("MainActivity", "Recording started successfully")

        }catch (e:Exception){
            Log.e("MainActivity", "Error during recording: ${e.message}", e)
        }
    }


    private suspend fun urconvertPcmToWav(pcmFile: File, wavFile: File,bufferSize: Int,  sampleRate: Int, channels: Int) : Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val urdataSize = pcmFile.length().toInt()
                val urtotalSize = urdataSize + urheaderSize
                val urbyteRate = sampleRate * channels * (uraudioFormat / 8)

                val urwavBuffer = ByteBuffer.allocate(urheaderSize + urdataSize)
                urwavBuffer.order(ByteOrder.LITTLE_ENDIAN)

                // WAV 파일 헤더 작성
                urwavBuffer.put("RIFF".toByteArray(Charsets.US_ASCII)) // ChunkID
                urwavBuffer.putInt(urtotalSize - 8) // ChunkSize
                urwavBuffer.put("WAVE".toByteArray(Charsets.US_ASCII)) // Format
                urwavBuffer.put("fmt ".toByteArray(Charsets.US_ASCII)) // Subchunk1ID
                urwavBuffer.putInt(16) // Subchunk1Size
                urwavBuffer.putShort(1) // AudioFormat (PCM)
                urwavBuffer.putShort(1.toShort()) // NumChannels (모노)
                urwavBuffer.putInt(sampleRate) // SampleRate
                urwavBuffer.putInt(sampleRate * 2) // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
                urwavBuffer.putShort(2.toShort()) // BlockAlign (NumChannels * BitsPerSample/8)
                urwavBuffer.putShort(16.toShort()) // BitsPerSample (16비트)

                urwavBuffer.put("data".toByteArray(Charsets.US_ASCII)) // Subchunk2ID
                urwavBuffer.putInt(urdataSize) // Subchunk2Size

                // PCM 데이터 추가
                val urinputStream = FileInputStream(pcmFile).channel
                urinputStream.read(urwavBuffer)
                urinputStream.close()

                urwavBuffer.flip()

                // WAV 파일에 데이터 쓰기
                val fileChannel = FileOutputStream(wavFile).channel
                fileChannel.write(urwavBuffer)
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
                    urstartRecording()
                } else {
                    // 권한이 거부되었을 때 처리
                }
            }
        }
    }

    private fun urstopRecording() = CoroutineScope(Dispatchers.IO).launch {
        urisRecording = false
        withContext(Dispatchers.Main) {
            urstate = State.AFTER_RECORDING
            urrecordButton.updateIconWithState(urstate)
            urrecordTimeTextView.stopCountUp()
        }
    }

    private fun urstartPlaying() = CoroutineScope(Dispatchers.IO).launch {
        urrecordingFile?.let { file ->
            if (file.exists()) {
                urisPlaying = true
                withContext(Dispatchers.Main) {
                    urstate = State.ON_PLAYING
                    urrecordButton.updateIconWithState(urstate)
                    urrecordTimeTextView.startCountUp()
                }

                val urinputStream = FileInputStream(file)
                val urbuffer = ByteArray(urbufferSize)
                uraudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, ursamplingRate, AudioFormat.CHANNEL_OUT_MONO, uraudioFormat, urbufferSize, AudioTrack.MODE_STREAM)

                uraudioTrack?.play()
                var read: Int
                while (urisPlaying) {
                    read = urinputStream.read(urbuffer)
                    if (read == -1) break
                    uraudioTrack?.write(urbuffer, 0, read)
                }

                uraudioTrack?.stop()
                uraudioTrack?.release()
                urinputStream.close()

                withContext(Dispatchers.Main) {
                    if (urisPlaying) {
                        urstopPlaying()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    // 파일이 존재하지 않을 때 처리
                    Toast.makeText(this@AudioRecording, "녹음된 파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()

                }
            }
        } ?: run {
            withContext(Dispatchers.Main) {
                // 파일이 null일 때 처리
                Toast.makeText(this@AudioRecording, "녹음 파일이 없습니다.", Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun urstopPlaying() = CoroutineScope(Dispatchers.IO).launch {
        urisPlaying = false
        withContext(Dispatchers.Main) {
            urstate = State.AFTER_RECORDING
            urrecordButton.updateIconWithState(urstate)
            urrecordTimeTextView.stopCountUp()
        }
    }

    private fun urresetRecording() = CoroutineScope(Dispatchers.IO).launch {
        urstopPlaying()
        urrecordingFile?.delete()
        urrecordingFile = null
        withContext(Dispatchers.Main) {
            urrecordTimeTextView.clearCountTime()
            urstate = State.BEFORE_RECORDING
            urconfirmButton.isEnabled = false
        }
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}