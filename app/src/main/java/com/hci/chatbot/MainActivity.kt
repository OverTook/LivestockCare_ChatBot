package com.hci.chatbot

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.hci.chatbot.tab.ChatFragment
import com.hci.chatbot.tab.MapFragment
import com.hci.chatbot.tab.ViewPagerAdapter
import com.hci.chatbot.utils.DoubleBackPressHandler
import com.hci.chatbot.utils.SharedPreferenceManager
import com.hci.chatbot.utils.TutorialUtil


class MainActivity : AppCompatActivity() {

    private val doubleBackPressHandler = DoubleBackPressHandler(this) //뒤로 두번

    private var mAuth: FirebaseAuth? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>
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

        tabLayoutInit()
        sidebarInit()

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {
            if(it[Manifest.permission.ACCESS_COARSE_LOCATION]!! && !it[Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                //대략적 위치
                Snackbar.make(findViewById(R.id.main), "정확한 위치 정보를 받아올 수 없어 정확성이 떨어집니다.", Snackbar.LENGTH_SHORT).show()
            }
            (supportFragmentManager.fragments[0] as MapFragment).getCurrentLocation()
        }

        doubleBackPressHandler.enable(drawerLayout)
    }

    fun sidebarInit() {
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
                // 정기 결제
                R.id.purchase_btn -> {
                    val billingDialog = BillingDialog(this, this)
                    billingDialog.show()
                    true
                }

                // 개인정보처리방침
                R.id.terms_btn -> {
                    var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://raw.githubusercontent.com/OverTook/LivestockCare_ChatBot/main/PrivacyPolicy.html"))
                    startActivity(intent)
                    true
                }

                // 앱 정보
                R.id.info_btn -> {
                    val customDialog = InfoDialog(this)
                    customDialog.show()
                    true
                }
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
    }

    fun tabLayoutInit() {
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

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    val fragment = supportFragmentManager.fragments[viewPager.currentItem]
                    val tutorialUtil = TutorialUtil.getInstance()
                    
                    if(viewPager.currentItem == 1 && tutorialUtil.chatTutorialing) {
                        findViewById<ImageButton>(R.id.menu_btn).isEnabled = false //메뉴 버튼 비활
                        
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED) //메뉴 스와이프 비활
                        
                        viewPager.isUserInputEnabled = false //스와이프 비활

                        val tabStrip = (tabLayout.getChildAt(0) as LinearLayout)
                        tabStrip.isEnabled = false //탭 메뉴 비활
                        for (i in 0 until tabStrip.childCount) {
                            tabStrip.getChildAt(i).isClickable = false //탭 메뉴 비활
                        }

                        fragment?.requireView()!!.tag = fragment as ChatFragment
                        tutorialUtil.tutorialChat(this@MainActivity, fragment.requireView()) {
                            findViewById<ImageButton>(R.id.menu_btn).isEnabled = true //메뉴 버튼

                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) //메뉴 스와이프

                            viewPager.isUserInputEnabled = true //스와이프

                            tabStrip.isEnabled = true //탭 메뉴
                            for (i in 0 until tabStrip.childCount) {
                                tabStrip.getChildAt(i).isClickable = true //탭 메뉴
                            }

                            SharedPreferenceManager(this@MainActivity).saveFirstCheckChat()
                        }
                    } else if(viewPager.currentItem == 0) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                            activityResultLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(findViewById(R.id.main), "정확한 위치 정보를 받아올 수 없어 정확성이 떨어집니다.", Snackbar.LENGTH_SHORT).show()
                        }

                        if(tutorialUtil.mapTutorialing) {
                            findViewById<ImageButton>(R.id.menu_btn).isEnabled = false //메뉴 버튼 비활

                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); //메뉴 스와이프 비활

                            viewPager.isUserInputEnabled = false //스와이프 비활
                            val tabStrip = (tabLayout.getChildAt(0) as LinearLayout)
                            tabStrip.isEnabled = false //탭 메뉴 비활
                            for (i in 0 until tabStrip.childCount) {
                                tabStrip.getChildAt(i).isClickable = false //탭 메뉴 비활
                            }

                            fragment?.requireView()!!.tag = fragment as MapFragment
                            tutorialUtil.tutorialMap(this@MainActivity, fragment.requireView()) {
                                findViewById<ImageButton>(R.id.menu_btn).isEnabled = true //메뉴 버튼

                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) //메뉴 스와이프

                                //viewPager.isUserInputEnabled = true //지도에서는 스와이프는 활성화하면 안된다.

                                tabStrip.isEnabled = true //탭 메뉴
                                for (i in 0 until tabStrip.childCount) {
                                    tabStrip.getChildAt(i).isClickable = true //탭 메뉴
                                }

                                SharedPreferenceManager(this@MainActivity).saveFirstCheckMap()
                            }
                        }
                    }
                }
            }
        })

        tabLayout.getTabAt(1)!!.select() //처음 시작은 챗봇 상태
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                if (tab.position == 1) {
                    viewPager.isUserInputEnabled = true //카카오맵 드래그를 위해
                    return
                }

                viewPager.isUserInputEnabled = false
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
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
    }

    override fun onResume() {
        super.onResume()
        if(mAuth!!.currentUser == null) {
            startActivity(
                Intent(this@MainActivity,
                LoginActivity::class.java)
            )
        }
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