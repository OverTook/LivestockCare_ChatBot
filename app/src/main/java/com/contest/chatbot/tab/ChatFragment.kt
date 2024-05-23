package com.contest.chatbot.tab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contest.chatbot.R
import com.contest.chatbot.chatui.CustomAdapter
import com.contest.chatbot.chatui.Item
import com.contest.chatbot.chatui.ViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ChatFragment : Fragment(), View.OnClickListener {

    private lateinit var viewOfLayout: View

    private var dataList: ArrayList<Item> = ArrayList<Item>();
    private lateinit var adapter: CustomAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Default)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.fragment_chat, container, false);

        initializeData()

        Log.e("STARTED", "CHAT")
        val btn: ImageButton = viewOfLayout.findViewById(R.id.chat_send_btn)
        btn.setOnClickListener(this);

        return viewOfLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = viewOfLayout.findViewById(R.id.chat_list_recycler)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        adapter = CustomAdapter(dataList, recyclerView)
        recyclerView.setAdapter(adapter)
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.chat_send_btn -> {
                val input: EditText = viewOfLayout.findViewById(R.id.chat_input)
                sendAndWait(input.text.toString())
                input.setText("")
            }
        }
    }
}