package com.hci.chatbot

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.hci.chatbot.tab.ViewPagerAdapter
import com.hci.chatbot.utils.DoubleBackPressHandler
import com.hci.chatbot.utils.SharedPreferenceManager


class MainActivity : AppCompatActivity() {

    private val doubleBackPressHandler = DoubleBackPressHandler(this) //뒤로 두번

    private var mAuth: FirebaseAuth? = null

    private lateinit var drawerLayout: DrawerLayout

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_with_sidebar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = FirebaseAuth.getInstance()

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


//        val showDialogButton = findViewById<ImageButton>(R.id.menu_btn)
//        showDialogButton.setOnClickListener {
//            val customDialog = InfoDialog(this)
//            customDialog.show()
//        }

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        findViewById<ImageButton>(R.id.menu_btn).setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        val profileManager = SharedPreferenceManager(this)
        val imageURL = profileManager.getImageURL()
        val headerView = navView.getHeaderView(0)
        if (!imageURL.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageURL)
                .into(headerView.findViewById(R.id.profileImage))
        }
        headerView.findViewById<TextView>(R.id.profileNickname).text = profileManager.getNickname()?: "기본 닉네임"
        headerView.findViewById<TextView>(R.id.profileEmail).text = profileManager.getEmail()?: "이메일 알 수 없음"

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navView_logout -> {
                    mAuth!!.signOut()
                    val intent = Intent(
                        this@MainActivity,
                        LoginActivity::class.java
                    )
                    startActivity(intent)

                    true
                }
                else -> false
            }
        }

        doubleBackPressHandler.enable(drawerLayout)
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