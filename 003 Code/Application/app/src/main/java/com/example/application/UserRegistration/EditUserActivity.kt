package com.example.application.UserRegistration

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.framerun.databinding.ActivityEditUserBinding
import com.example.framerun.postUsername
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class EditUserActivity : AppCompatActivity() {

    //리스트는 리사이클러뷰에 표시될 이미지의 URI를 저장하는 역할
    var list = ArrayList<Uri>()
    //MultiImageAdapter 클래스의 인스턴스를 생성
    //list는 이미지 URI 리스트를, this는 현재 액티비티(또는 컨텍스트)를 의미
    //리사이클러뷰의 아이템을 관리하고 표시하는 역할
    val adapter = MultiImageAdapter(list, this)

    lateinit var binding: ActivityEditUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱에서 외부 저장소에 접근하기 위해 사용자로부터 권한을 요청하는 부분
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

        //액티비티 전환할 때 putExtra로 type 정보 함께 보냄
        val type = intent.getStringExtra("type")

        if (type.equals("ADD")) {
            binding.btnSave.text = "추가하기"
        } else {
            binding.btnSave.text = "수정하기"
        }


        //이미지 가져오기 버튼 클릭 리스너
        binding.getImage.setOnClickListener {

            //이미지 선택 위한 액션 설정한 객체 생성
            //Intent.ACTION_PICK -> 데이터 선택하는 액션
            var intent = Intent(Intent.ACTION_PICK)

            //이미지 데이터 가져올 위치를 설정
            //MediaStore.Images.Media.EXTERNAL_CONTENT_URI -> 외부 저장소에 있는 이미지를 가리키는 uri
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            //다중 선택을 허용하기 위한 추가정보를 설정
            //true로 설정하여 다중 선택 활성화
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            //액션을 컨텐츠 가져오기로 설정
            intent.action = Intent.ACTION_GET_CONTENT

            //액티비티를 실행하여 이미지를 선택하고 결과를 받기위해 호출, onActivityResult()에서 결과 처리
            //intent -> 실행할 액티비티와 관련된 정보 포함
            //200 -> 요청코드
            startActivityForResult(intent, 200)
        }

        //수평방향으로 아이템 배치하는 레이아웃 매니저 생성, false-> 역순스크롤 비활성화
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        //리사이클러뷰의 레이아웃 매니저를 설정
        binding.recyclerView.layoutManager = layoutManager
        //리사이클러뷰에 사용할 어댑터 설정
        binding.recyclerView.adapter = adapter

        //save 버튼 클릭 리스너
        binding.btnSave.setOnClickListener {
            //입력된 제목 가져오기
            val title = binding.UserTitle.text.toString()
            //입력된 내용 가져오기
            val content = binding.UserContent.text.toString()
            // 현재 시간을 yyyy-MM-dd HH:mm 형식으로 포맷하여 가져오기
            val currentDate =
                SimpleDateFormat("yyyy-MM-dd HH:mm").format(System.currentTimeMillis())


            //입력된 제목, 내용, 날짜 등으로 User 객체 생성
            val user = User(0, title, content, currentDate, false)

            //type add인 경우
            if (type.equals("ADD")) {
                //제목과 내용이 모두 비어있지 않는경우 확인
                if (title.isNotEmpty() && content.isNotEmpty()) {
                    //user 객체 생성
                    val user = User(0, title, content, currentDate, false)
                    //결과로 반환할 intent 객체 생성 및 User객체와 flag 값 추가
                    val intent = Intent().apply {
                        putExtra("user", user)
                        putExtra("flag", 0)
                    }
                    //결과 코드와 함께 intent를 설정
                    setResult(RESULT_OK, intent)

                    // 이미지 파이어베이스 스토리지에 데이터 저장
                    uploadDataToFirebase(user, list)
                    //글로벌 스코프에서 코루틴을 실행
                    //화면전환이 딜레이시간만큼 안되는 현상을 막기위해
                    //애플리케이션 전역에서 실행되는 독립적인 백그라운드 작업으로 실행
                    GlobalScope.launch {
                        //스토리지에 이미지 파일을 전송한 후 파이어베이스에 업로드 되는데까지
                        //시간이 걸리는데 서버로 바로 신호를 주면 이미지 파일이 없는 오류가 발생
                        //그래서 delay를 주고 서버로 신호 전송
                        delay(15000)
                        // 서버로 신호를 보내는 부분
                        postUsername(user.title)
                    }
                    finish()

                }
            } else {
                // 수정
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //돌아온 결과가 성공적이고 요청코드가 200인 경우 확인
        if (resultCode == RESULT_OK && requestCode == 200) {
            //이미지 URI 담고 있는 list 초기화
            list.clear()

            if (data?.clipData != null) { // 사진 여러개 선택한 경우
                //선택한 사진의 개수 가져옴
                val count = data.clipData!!.itemCount
                //20장 초과 시 메시지 표시하고 함수 종료
                if (count > 20) {
                    Toast.makeText(applicationContext, "사진은 20장까지 선택 가능합니다.", Toast.LENGTH_LONG)
                    return
                }
                //선택한 각 사진에 대해 반복문 실행
                for (i in 0 until count) {
                    //i번째 이미지의 URI를 가져옴
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    //리스트에 추가
                    list.add(imageUri)
                }
            } else { // 단일 선택
                data?.data?.let { uri ->
                    //선택 이미지 uri 가져오기
                    val imageUri: Uri? = data?.data
                    //사진의 uri가 null이 아닌 경우에만 처리
                    if (imageUri != null) {
                        //리스트 추가
                        list.add(imageUri)
                    }
                }
            }
            //어댑터에 변경사항 알려 리사이클러뷰 갱신
            adapter.notifyDataSetChanged()

        }

    }

    //스토리지에 사진 올리기
    private fun uploadDataToFirebase(user: User, imageList: ArrayList<Uri>) {

        //스토리지 초기화
        //파이어베이스 스토리지 인스턴스 가져오기
        val storage = FirebaseStorage.getInstance()
        //파이어베이스 스토리지의 루트 참조 가져오기
        val storageRef = storage.reference

        //업로드된 이미지의 다운로드 URL 저장하기 위한 빈문자열 배열 초기화
        val imageUrls = ArrayList<String>()
        //이미지 업로드 작업을 저장하기 위한 빈 작업 목록 초기화
        val uploadTasks = mutableListOf<Task<Uri>>()

        //사진 순서대로 보내는 코드
        for (i in 0 until imageList.size) {
            //현재 순회중인 이미지의 uri 가져옴
            val imageUri = imageList[i]
            //업로드할 이미지의 이름 생성
            val imageName = "${user.title}/image_${System.currentTimeMillis()}_$i.jpg"

            //이미지를 업로드할 파이어베이스 스토리지 내의 경로 지정
            val imagesRef = storageRef.child(imageName)
            //이미지를 업로드하는 작업 생성
            val uploadTask = imagesRef.putFile(imageUri)

            //이미지 업로드 작업 완료시 다운로드 url을 가져오는 작업 생성
            val completionTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imagesRef.downloadUrl
            }

            //이미지 업로드가 성공한 경우 처리할 작업을 정의
            completionTask.addOnSuccessListener { uri ->
                // 이미지 업로드 성공 시 다운로드 URL 저장
                val imageUrl = uri.toString()
                imageUrls.add(imageUrl)

            }

            completionTask.addOnFailureListener { exception ->
                // 이미지 업로드 실패 시 처리
                Log.e(TAG, "Image upload failed.", exception)
            }
            uploadTasks.add(completionTask)
        }

    }

    companion object {
        private const val TAG = "EditUserActivity"
    }


}