package com.contest.chatbot.tab

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.contest.chatbot.ChatRequest
import com.contest.chatbot.ChatResponse
import com.contest.chatbot.HistoryData
import com.contest.chatbot.NetworkManager
import com.contest.chatbot.R
import com.contest.chatbot.chatui.CustomAdapter
import com.contest.chatbot.chatui.Item
import com.contest.chatbot.chatui.ViewType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ChatFragment : Fragment(), View.OnClickListener {

    private val dataList: ArrayList<Item> = ArrayList()

    private var waitForResponse: Boolean = false
    private var chatHistory: ArrayList<HistoryData> = ArrayList()

    private lateinit var imm: InputMethodManager
    private lateinit var viewOfLayout: View
    private lateinit var adapter: CustomAdapter

    private var startedChat: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.fragment_chat, container, false);

        val maskView: View = viewOfLayout.findViewById(R.id.mask_view)
        maskView.setOnClickListener(this)

        val btn: ImageButton = viewOfLayout.findViewById(R.id.chat_send_btn)
        btn.setOnClickListener(this)

        val anim: LottieAnimationView = viewOfLayout.findViewById(R.id.hello_animation)
        anim.setOnClickListener(this)

        return viewOfLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = viewOfLayout.findViewById(R.id.chat_list_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = CustomAdapter(dataList, recyclerView)
        recyclerView.setAdapter(adapter)

        imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        //키 Enter 이벤트
        val input: EditText = viewOfLayout.findViewById(R.id.chat_input)
        input.setOnKeyListener(View.OnKeyListener { _, keyCode, event -> //Enter key Action
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)

                if(input.text.isNullOrBlank()) {
                    return@OnKeyListener true
                }
                //처리
                if(waitForResponse)
                    return@OnKeyListener true

                sendAndWait(input.text.toString())
                input.setText("")

                return@OnKeyListener true
            }
            false
        })


    }

    private fun sendAndWait(msg: String) {
        if(!startedChat) {
            val parent = viewOfLayout.findViewById<RelativeLayout>(R.id.no_text_alert_layout)
            val root = parent.parent as RelativeLayout
            root.removeView(parent)
        }

        startedChat = true
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

                chatHistory = body.history as ArrayList<HistoryData>

                adapter.addItem(Item(body.msg, getCurrentTime(), ViewType.LEFT_CHAT))
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
        Log.e("WHO", v!!.id.toString())
        when (v!!.id) {
            R.id.chat_send_btn -> {
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)

                val input: EditText = viewOfLayout.findViewById(R.id.chat_input)
                if(input.text.isNullOrBlank()) {
                    return
                }

                if(waitForResponse)
                    return

                sendAndWait(input.text.toString())
                input.setText("")
            }
            R.id.mask_view -> {
                viewOfLayout.findViewById<EditText>(R.id.chat_input).requestFocus()
                imm.showSoftInput(viewOfLayout.findViewById<EditText>(R.id.chat_input), android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
            R.id.hello_animation -> {
                sendAndWait("안녕하세요!")
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}