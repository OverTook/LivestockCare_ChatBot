package com.contest.chatbot

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.contest.chatbot.handler.DoubleBackPressHandler
import com.contest.chatbot.tab.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.kakao.vectormap.KakaoMapSdk


class MainActivity : AppCompatActivity() {

    private val doubleBackPressHandler = DoubleBackPressHandler(this) //뒤로 두번

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        KakaoMapSdk.init(this, "4970ef36f0902a772d347b75b15e8907");

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val adapter = ViewPagerAdapter(this)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = adapter
        viewPager.reduceDragSensitivity() //감도 조절


        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab, position ->
            when (position) {
                0 -> tab.setText("               MAP               ") //일부러 공백을 줌
                1 -> tab.setText("              CHAT              ")
            }
        }.attach()

        tabLayout.getTabAt(1)!!.select() //처음 시작은 챗봇 상태

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                if (tab.position == 0) {
                    viewPager.isUserInputEnabled = false //카카오맵 드래그를 위해
                } else if (tab.position == 1) {
                    viewPager.isUserInputEnabled = true
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                //
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                //
            }
        })

        val root = findViewById<ConstraintLayout>(R.id.main);
        root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private var isKeyboardShowing = false

            override fun onGlobalLayout() {
                val rect = Rect()
                root.getWindowVisibleDisplayFrame(rect)
                val screenHeight = root.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                // 키보드가 올라왔을 때
                if (keypadHeight > screenHeight * 0.15) {
                    if (!isKeyboardShowing) {
                        isKeyboardShowing = true
                        viewPager.isUserInputEnabled = false //키보드가 올라와 있을 때에는 드래그 방지
                    }
                } else {
                    if (isKeyboardShowing) {
                        isKeyboardShowing = false
                        viewPager.isUserInputEnabled = true //키보드가 사라졌으니 다시 드래그 허용
                    }
                }
            }
        })


        val showDialogButton = findViewById<ImageButton>(R.id.info_btn)
        showDialogButton.setOnClickListener {
            val customDialog = InfoDialog(this)
            customDialog.show()
        }
        doubleBackPressHandler.enable()
    }

    override fun onDestroy() {
        super.onDestroy()
        doubleBackPressHandler.disable()
    }

    private fun ViewPager2.reduceDragSensitivity(f: Int = 4) {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop*f) //8기본
    }
}