package com.contest.chatbot.tab

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.contest.chatbot.DiseaseListResponse
import com.contest.chatbot.MainActivity
import com.contest.chatbot.NetworkManager
import com.contest.chatbot.R
import com.contest.chatbot.maps.BottomSheetList
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapFragment : Fragment() {

    private lateinit var viewOfLayout: View
    private lateinit var mapView: MapView
    private lateinit var kakaoMap: KakaoMap
    private lateinit var supportFragmentManager: FragmentManager
    private lateinit var diseaseListManager: BottomSheetList

    //질병 목록 조회, 다른 목록 요청시 취소하기 위해 전역 변수로 캐싱
    private var lastCallDisease: Call<DiseaseListResponse>? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = viewOfLayout.findViewById(R.id.kakaoMap)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {

            }

            override fun onMapError(error: Exception) {

            }

        }, object : KakaoMapReadyCallback() {

            override fun onMapReady(kakaoMapReady: KakaoMap) {
                kakaoMap = kakaoMapReady

                kakaoMap.setOnViewportClickListener(object : KakaoMap.OnViewportClickListener {
                    override fun onViewportClicked(p0: KakaoMap?, p1: LatLng?, p2: PointF?) {
                        if (p1 == null) return

                        showDisease(p1)
                    }

                })
            }
        })

        supportFragmentManager = (activity as MainActivity).supportFragmentManager
        diseaseListManager = BottomSheetList()

        return viewOfLayout
    }


    fun showDisease(pos: LatLng) {
        lastCallDisease?.cancel() //이전 요청은 취소한다.

        lastCallDisease = NetworkManager.apiService.getDiseaseData(pos.latitude, pos.longitude, kakaoMap.zoomLevel)
        lastCallDisease?.enqueue(object : Callback<DiseaseListResponse> {
            override fun onResponse(call: Call<DiseaseListResponse>, response: Response<DiseaseListResponse>) {
                if(call.isCanceled) {
                    Log.e("Network", "취소된 요청입니다.")
                    return
                }

                if (!response.isSuccessful) {
                    Log.e("Network", "요청에 실패했습니다.")
                    return
                }

                val data = response.body()!!.data
                if(data.diseaseList.isEmpty()) { //현재 요청이 이전 요청이거나 목록이 비어있으면 더 진행하지 않는다
                    return
                }

                diseaseListManager.setData(data.address, data.diseaseList)
                diseaseListManager.show(supportFragmentManager, diseaseListManager.tag)

                lastCallDisease = null //요청 처리 완료되었으니 비워준다.
            }

            override fun onFailure(call: Call<DiseaseListResponse>, t: Throwable) {
                //오류 처리
                Log.e("Network", "요청에 실패했습니다. $t")

                lastCallDisease = null //요청 처리 완료되었으니 비워준다.
            }
        })
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