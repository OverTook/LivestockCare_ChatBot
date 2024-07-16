package com.hci.chatbot.utils

import android.view.Gravity
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class DoubleBackPressHandler(private val activity: ComponentActivity) {

    private var doubleBackToExitPressedOnce = false
    private var drawerLayout: DrawerLayout? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (drawerLayout != null && drawerLayout!!.isDrawerOpen(GravityCompat.END)) {
                drawerLayout!!.closeDrawers()
                return
            }

            if (doubleBackToExitPressedOnce) {
                activity.finishAffinity()
                exitProcess(0);
            } else {
                doubleBackToExitPressedOnce = true
                //activity.toast("Press back again to exit")
                Toast.makeText(activity, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
                activity.lifecycleScope.launch {
                    delay(2000)
                    doubleBackToExitPressedOnce = false
                }
            }
        }
    }

    fun enable(drawerLayout: DrawerLayout? = null) {
        this.drawerLayout = drawerLayout
        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    fun disable() {
        onBackPressedCallback.remove()
    }
}