package com.contest.chatbot

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.contest.chatbot.tab.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.kakao.vectormap.KakaoMapSdk


class MainActivity : AppCompatActivity() {

    private var initCount = 2

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        KakaoMapSdk.init(this, "560c5e8da1c129776a0318780f018d7c");

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab, position ->
            when (position) {
                0 -> tab.setText("               MAP               ") //일부러 공백을 줌
                1 -> tab.setText("              CHAT              ")
            }
        }.attach()

        tabLayout.getTabAt(1)!!.select()

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
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
    }

    fun initEnd() {
        initCount--
        if (initCount != 0)
            return

        val anim = findViewById<LottieAnimationView>(R.id.init_loading)
        //(anim.parent as ViewGroup).removeView(anim)
        anim.visibility = View.GONE
    }
}