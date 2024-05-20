package com.contest.chatbot

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.contest.chatbot.chatui.CustomAdapter
import com.contest.chatbot.chatui.Item
import com.contest.chatbot.chatui.ViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private var dataList: ArrayList<Item> = ArrayList<Item>();
    private lateinit var adapter: CustomAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        this.initializeData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        adapter = CustomAdapter(dataList, recyclerView)
        recyclerView.setAdapter(adapter)

        val btn: ImageButton = findViewById(R.id.imageButton)
        val input: EditText = findViewById(R.id.editTextText)
        btn.setOnClickListener {
            sendAndWait(input.text.toString())
            input.setText("")
        }

        //TODO 테스트용 코드
        NetworkManager.apiService.getData(37.0047135, 127.2988789, 5).enqueue(object : Callback<ClusteringResponse> {
            override fun onResponse(call: Call<ClusteringResponse>, response: Response<ClusteringResponse>) {
                if(!response.isSuccessful)
                    return;

                val result = response.body() ?: return;

                Log.e("RESULT", result.Data[0].Lat);
                Log.e("RESULT", result.Data[0].Lng);
                Log.e("RESULT", result.Data[0].AddressCode);
                Log.e("RESULT", result.Data[0].AddressName);
                Log.e("RESULT", result.Data[0].Alpha)
                Log.e("RESULT", result.Data[0].TotalOccurCount);
            }

            override fun onFailure(call: Call<ClusteringResponse>, err: Throwable) {
                Log.d("RESULT", "통신 오류 발생");
                Log.d("RESULT", err.toString());
            }
        }) ;

    }

    private fun sendAndWait(msg: String) {
        adapter.addItem(Item(msg, "", getCurrentTime(), ViewType.RIGHT_CHAT))
        coroutineScope.launch {
            delay(3000)
            withContext(Dispatchers.Main) {
                adapter.addItem(Item(msg + "에 대한 답장이에요", "", getCurrentTime(), ViewType.LEFT_CHAT))
            }
        }
    }

    private fun initializeData() {
        dataList.add(Item("안녕하세요 HCI연구팀 공모전 참여하는 김도환, 송주훈, 고건호, 한신영 팀입니다.", "", "8:31 AM", ViewType.RIGHT_CHAT))
        dataList.add(Item("안녕하세요. 이것은 테스트용 답변입니다. 반갑습니다.", "", "null", ViewType.LEFT_CHAT))
        dataList.add(Item("안녕하세요", "null", "오후 2:01", ViewType.RIGHT_CHAT))
        dataList.add(Item("안녕하세요", "스틱코드", "오후 2:00", ViewType.LEFT_CHAT))
        dataList.add(Item("안녕하세요", "null", "오후 2:01", ViewType.RIGHT_CHAT))
        dataList.add(Item("안녕하세요", "스틱코드", "오후 2:00", ViewType.LEFT_CHAT))
        dataList.add(Item("안녕하세요", "null", "오후 2:01", ViewType.RIGHT_CHAT))
        dataList.add(Item("안녕하세요", "스틱코드", "오후 2:00", ViewType.LEFT_CHAT))
        dataList.add(Item("안녕하세요", "null", "오후 2:01", ViewType.RIGHT_CHAT))
        dataList.add(Item("안녕하세요", "스틱코드", "오후 2:00", ViewType.LEFT_CHAT))
        dataList.add(Item("안녕하세요", "null", "오후 2:01", ViewType.RIGHT_CHAT))
        dataList.add(Item("안녕하세요", "스틱코드", "오후 2:00", ViewType.LEFT_CHAT))
        dataList.add(Item("안녕하세요", "null", "오후 2:01", ViewType.RIGHT_CHAT))
        dataList.add(Item("안녕하세요", "스틱코드", "오후 2:00", ViewType.LEFT_CHAT))
        dataList.add(Item("안녕하세요", "null", "오후 2:01", ViewType.RIGHT_CHAT))
    }

    fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())
        return dateFormat.format(currentTime)
    }
}