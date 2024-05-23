package com.contest.chatbot.tab

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.contest.chatbot.MainActivity
import com.contest.chatbot.R
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.Poi
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.mapwidget.MapWidget


class MapFragment : Fragment() {

    private lateinit var viewOfLayout: View
    private lateinit var mapView: MapView
    private lateinit var kakaoMap: KakaoMap
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(com.contest.chatbot.R.layout.fragment_map, container, false)

        mapView = viewOfLayout.findViewById(R.id.kakaoMap)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {

            }

            override fun onMapError(error: Exception) {

            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMapReady: KakaoMap) {
                kakaoMap = kakaoMapReady
            }
        })
        return viewOfLayout
    }


    override fun onResume() {
        super.onResume()
        mapView.resume() // MapView 의 resume 호출
    }

    override fun onPause() {
        super.onPause()
        mapView.pause() // MapView 의 pause 호출
    }
}