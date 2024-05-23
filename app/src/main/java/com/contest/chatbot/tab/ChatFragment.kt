package com.contest.chatbot.tab

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contest.chatbot.ChatRequest
import com.contest.chatbot.ChatResponse
import com.contest.chatbot.HistoryData
import com.contest.chatbot.MainActivity
import com.contest.chatbot.NetworkManager
import com.contest.chatbot.R
import com.contest.chatbot.chatui.CustomAdapter
import com.contest.chatbot.chatui.Item
import com.contest.chatbot.chatui.ViewType
import com.contest.chatbot.history.ChatHistoryManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ChatFragment : Fragment(), View.OnClickListener {

    private val dataList: ArrayList<Item> = ArrayList<Item>()

    private var waitForResponse: Boolean = false

    private lateinit var historyManager: ChatHistoryManager
    private lateinit var imm: InputMethodManager
    private lateinit var viewOfLayout: View
    private lateinit var adapter: CustomAdapter
    private lateinit var chatHistory: List<HistoryData>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.fragment_chat, container, false);

        historyManager = ChatHistoryManager(requireContext())
        chatHistory = if(historyManager.loadHistoryList() != null) {
            historyManager.loadHistoryList()!!
        } else {
            ArrayList()
        }

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

        imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        //키 Enter 이벤트
        val input: EditText = viewOfLayout.findViewById(R.id.chat_input)
        input.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> //Enter key Action
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)


                //처리
                if(waitForResponse)
                    return@OnKeyListener true

                sendAndWait(input.text.toString())
                input.setText("")

                return@OnKeyListener true
            }
            false
        })

        //채팅 목록 불러오기
        chatHistory.forEach { history ->
            if(history.role == "user") {
                adapter.addItem(Item(history.content, history.timestamp, ViewType.RIGHT_CHAT))
            } else {
                adapter.addItem(Item(history.content, history.timestamp, ViewType.LEFT_CHAT))
            }
        }

        (activity as MainActivity).initEnd()
    }

    private fun sendAndWait(msg: String) {
        adapter.addItem(Item(msg, getCurrentTime(), ViewType.RIGHT_CHAT))
        waitForResponse = true
        NetworkManager.apiService.chat(ChatRequest(msg, chatHistory)).enqueue(object :
            Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                waitForResponse = false

                if(!response.isSuccessful) {
                    adapter.addItem(Item("요청이 올바르지 않습니다.", getCurrentTime(), ViewType.LEFT_CHAT))
                    return
                }

                val body = response.body() ?: return

                chatHistory = body.history
                adapter.addItem(Item(body.msg, getCurrentTime(), ViewType.LEFT_CHAT))

                historyManager.saveHistoryList(chatHistory)
            }

            override fun onFailure(call: Call<ChatResponse>, err: Throwable) {
                adapter.addItem(Item("서버 연결에 실패하였습니다.", getCurrentTime(), ViewType.LEFT_CHAT))
            }

        })
    }

    fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())
        return dateFormat.format(currentTime)
    }

    override fun onClick(v: View?) {
        //버튼으로 보내는 부분
        when (v!!.id) {
            R.id.chat_send_btn -> {
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)

                if(waitForResponse)
                    return

                val input: EditText = viewOfLayout.findViewById(R.id.chat_input)
                sendAndWait(input.text.toString())
                input.setText("")
            }
        }
    }
}