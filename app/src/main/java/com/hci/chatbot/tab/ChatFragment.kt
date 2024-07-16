package com.hci.chatbot.tab

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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.hci.chatbot.R
import com.hci.chatbot.chatui.CustomAdapter
import com.hci.chatbot.chatui.Item
import com.hci.chatbot.chatui.ViewType
import com.hci.chatbot.network.ChatRequest
import com.hci.chatbot.network.ChatResponse
import com.hci.chatbot.network.HistoryData
import com.hci.chatbot.network.NetworkManager
import com.hci.chatbot.network.ValidChatCountResponse
import com.google.android.material.snackbar.Snackbar
import com.hci.chatbot.utils.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ChatFragment : Fragment(), View.OnClickListener {

    private val dataList: ArrayList<Item> = ArrayList()
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    private var waitForResponse: Boolean = false
    private var chatHistory: ArrayList<HistoryData> = ArrayList()

    private lateinit var imm: InputMethodManager
    private lateinit var viewOfLayout: View
    private lateinit var adapter: CustomAdapter

    private var startedChat: Boolean = false

    private var maxChatCount: Int = -2
    private var curChatCount: Int = -2

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

        job = Job()
        sharedPreferenceManager = SharedPreferenceManager(requireActivity())
        getValidChatCount()

        return viewOfLayout
    }

    private fun getValidChatCount(){
        NetworkManager.apiService.getValidChatCount().enqueue(object : Callback<ValidChatCountResponse> {
            override fun onResponse(call: Call<ValidChatCountResponse>, response: Response<ValidChatCountResponse>) {
                if(!response.isSuccessful) {
                    setChatCount()
                    Snackbar.make(activity!!.findViewById(R.id.main), "일일 대화 횟수를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show();
                    return
                }

                if(response.body() == null) {
                    setChatCount()
                    Snackbar.make(activity!!.findViewById(R.id.main), "일일 대화 횟수를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show();
                    return
                }


                this@ChatFragment.curChatCount = response.body()!!.curCount
                this@ChatFragment.maxChatCount = response.body()!!.maxCount
                setChatCount()
            }

            override fun onFailure(call: Call<ValidChatCountResponse>, err: Throwable) {
                setChatCount()
                Snackbar.make(activity!!.findViewById(R.id.main), "일일 대화 횟수를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show();
            }
        })
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

                if(curChatCount <= 0) {
                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "금일 챗봇 이용 횟수가 소진되었습니다.", Snackbar.LENGTH_LONG)
                        .setAnchorView(viewOfLayout.findViewById(R.id.chat_input_background))
                        .show()
                    return@OnKeyListener true
                }

                sendAndWait(input.text.toString())
                input.setText("")

                return@OnKeyListener true
            }
            false
        })


    }

    private fun sendAndWait(msg: String) {
        if(this.curChatCount <= 0) {
            return
        }

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

                curChatCount--
                setChatCount()

                sharedPreferenceManager.saveLastChatTime(System.currentTimeMillis())
            }

            override fun onFailure(call: Call<ChatResponse>, err: Throwable) {
                adapter.addItem(Item("서버 연결에 실패하였습니다.", getCurrentTime(), ViewType.LEFT_CHAT))
            }

        })
    }

    fun setChatCount() {
        val sb = StringBuilder()
        sb.append("일일 대화 횟수: ")
        when(curChatCount) {
            -2 -> {
                sb.append("알 수 없음")
            }
            -1 -> {
                sb.append("제한 없음")
            }
            else -> {
                sb.append(curChatCount)
            }
        }
        sb.append(" / ")
        when(maxChatCount) {
            -2 -> {
                sb.append("알 수 없음")
            }
            -1 -> {
                sb.append("제한 없음")
            }
            else -> {
                sb.append(maxChatCount)
            }
        }

        viewOfLayout.findViewById<TextView>(R.id.chatCount).text = sb.toString()
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

                if(curChatCount <= 0) {
                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "금일 챗봇 이용 횟수가 소진되었습니다.", Snackbar.LENGTH_LONG)
                        .setAnchorView(viewOfLayout.findViewById(R.id.relativeLayout))
                        .show()
                    return
                }

                sendAndWait(input.text.toString())
                input.setText("")
            }
            R.id.mask_view -> {
                viewOfLayout.findViewById<EditText>(R.id.chat_input).requestFocus()
                imm.showSoftInput(viewOfLayout.findViewById<EditText>(R.id.chat_input), android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
            R.id.hello_animation -> {
                if(curChatCount <= 0) {
                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "금일 챗봇 이용 횟수가 소진되었습니다.", Snackbar.LENGTH_LONG)
                        .setAnchorView(viewOfLayout.findViewById(R.id.chat_input_background))
                        .show()
                    return
                }

                sendAndWait("안녕하세요!")
            }
        }
    }

    private lateinit var job: Job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onResume() {
        super.onResume()
        startMidnightJob()

        val lastSavedTimeMillis = sharedPreferenceManager.getLastChatTime()
        val currentTimeMillis = System.currentTimeMillis()

        val lastSavedDate = Date(lastSavedTimeMillis)
        val currentDate = Date(currentTimeMillis)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val lastSavedDateString = dateFormat.format(lastSavedDate)
        val currentDateString = dateFormat.format(currentDate)

        //string으로 비교하는게 비효율적일수도..

        if (lastSavedDateString != currentDateString) {
            getValidChatCount()
        }
    }

    private fun startMidnightJob() {
        val coroutineScope = CoroutineScope(Dispatchers.Main + job)

        coroutineScope.launch {
            // 현재 시간을 기준으로 다음 자정 시간을 계산
            val currentTime = Calendar.getInstance()
            val midnightTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1) // 다음 날 자정
            }

            // 다음 자정까지의 시간 계산
            val delayMillis = midnightTime.timeInMillis - currentTime.timeInMillis

            // 다음 자정까지 대기
            delay(delayMillis)

            getValidChatCount()
        }
    }
}