package com.contest.chatbot.tab

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PointF
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.contest.chatbot.CenterAddrResponse
import com.contest.chatbot.ClusteringResponse
import com.contest.chatbot.DiseaseListResponse
import com.contest.chatbot.MainActivity
import com.contest.chatbot.NetworkManager
import com.contest.chatbot.R
import com.contest.chatbot.maps.BottomSheetList
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.mapwidget.InfoWindow
import com.kakao.vectormap.mapwidget.InfoWindowOptions
import com.kakao.vectormap.mapwidget.component.GuiImage
import com.kakao.vectormap.mapwidget.component.GuiLayout
import com.kakao.vectormap.mapwidget.component.GuiText
import com.kakao.vectormap.mapwidget.component.Orientation
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.Polygon
import com.kakao.vectormap.shape.PolygonOptions
import com.kakao.vectormap.shape.PolygonStyle
import com.kakao.vectormap.shape.PolylineStyle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapFragment : Fragment() {

    private lateinit var viewOfLayout: View
    private lateinit var mapView: MapView
    private lateinit var kakaoMap: KakaoMap
    private lateinit var supportFragmentManager: FragmentManager
    private lateinit var diseaseListManager: BottomSheetList
    private val polygons = mutableListOf<Polygon>()     // Polygon 객체
    private val infoWindows = mutableListOf<InfoWindow>()     // Label 객체
    private var currCenterAddr = ""                     // 중심 좌표 문자열
    private var zoomlv = ""                             // 확대 레벨 문자열

    //질병 목록 조회, 다른 목록 요청시 취소하기 위해 전역 변수로 캐싱
    private var lastCallDisease: Call<DiseaseListResponse>? = null
    private var lastCallGeometry: Call<ClusteringResponse>? = null
    private var toast: Toast? = null
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
                // 지적도 받아오기
                getGeoPolygon()

                kakaoMap.setOnViewportClickListener(object : KakaoMap.OnViewportClickListener {
                    override fun onViewportClicked(p0: KakaoMap?, p1: LatLng?, p2: PointF?) {
                        if (p1 == null) return

                        showDisease(p1)
                    }

                })

                //카메라 이동이 있을 떄 지도이동이 끝난 후  카메라 값을 가져옴 (이벤트 등록)
                kakaoMap.setOnCameraMoveEndListener(object : KakaoMap.OnCameraMoveEndListener {
                    override fun onCameraMoveEnd(
                        kakaoMap: KakaoMap,
                        position: CameraPosition,
                        gestureType: GestureType,
                    ) {
                        getGeoPolygon()
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


        //diseaseListManager.setData("지역을 불러오는 중입니다.", ArrayList<DiseaseListItemData>())
        diseaseListManager.show(supportFragmentManager, diseaseListManager.tag)

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
                    diseaseListManager.dismiss()
                    toast?.cancel()
                    toast = Toast.makeText(requireContext(), "해당 지역은 질병 발생 정보가 없습니다.", Toast.LENGTH_SHORT)
                    toast?.show()
                    return
                }

                diseaseListManager.setData(buildString {
                    append(data.address)
                    append("의 질병 발생 현황")
                }, data.diseaseList)

                lastCallDisease = null //요청 처리 완료되었으니 비워준다.
            }

            override fun onFailure(call: Call<DiseaseListResponse>, t: Throwable) {
                //오류 처리
                Log.e("Network", "요청에 실패했습니다. $t")

                lastCallDisease = null //요청 처리 완료되었으니 비워준다.
            }
        })
    }

    fun getGeoPolygon(){
        var isChangeRegion = true
        val position = kakaoMap.cameraPosition!!.position //맵 이동시 그리는건 이런식으로

        NetworkManager.apiService.getCenterAddr(position.latitude, position.longitude, kakaoMap.zoomLevel).enqueue(object : Callback<CenterAddrResponse> {
            override fun onResponse(call: Call<CenterAddrResponse>, response: Response<CenterAddrResponse>) {
                if(!response.isSuccessful)
                    return

                val centerAddrResponse = response.body()
                if (centerAddrResponse != null) {
                    val addr = centerAddrResponse.addr
                    if (kakaoMap.zoomLevel <= 8) {
                        if (zoomlv !== "big") {
                            zoomlv = "big"
                            isChangeRegion = true
                        }
                        else isChangeRegion = false
                    } else if (kakaoMap.zoomLevel <= 12) {
                        if (zoomlv !== "medium") {
                            zoomlv = "medium"
                            isChangeRegion = true
                        }
                        else if (currCenterAddr != addr) {
                            currCenterAddr = addr
                            isChangeRegion = true
                        }
                        else isChangeRegion = false
                    } else {
                        if (zoomlv !== "small") {
                            zoomlv = "small"
                            isChangeRegion = true
                        }
                        else if (currCenterAddr != addr) {
                            currCenterAddr = addr
                            isChangeRegion = true
                        }
                        else isChangeRegion = false
                    }
                }
                Log.e("Result", "1 $zoomlv $currCenterAddr $isChangeRegion ${kakaoMap.zoomLevel}")

                if (isChangeRegion) {
                    lastCallGeometry?.cancel() //이전 요청은 취소한다.
                    lastCallGeometry = NetworkManager.apiService.getClusteredData(
                        position.latitude,
                        position.longitude,
                        kakaoMap.zoomLevel
                    )
                    lastCallGeometry?.enqueue(object : Callback<ClusteringResponse> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<ClusteringResponse>,
                            response: Response<ClusteringResponse>
                        ) {
                            if(call.isCanceled) {
                                Log.e("Network", "취소된 요청입니다.")
                                return
                            }
                            if (!response.isSuccessful)
                                return

                            val result = response.body() ?: return;
                            killPolygons()
                            killInfoWindows()
                            result.Data.forEach { data ->
                                val areaPoints = ArrayList<LatLng>()
                                val geometryList = data.geometry[0][0]
                                geometryList.forEach { geometry ->
                                    val lat = geometry[1]
                                    val lng = geometry[0]
                                    areaPoints.add(LatLng.from(lat, lng))
                                }

                                val polygonStyle = PolygonStyle.from(
                                    Color.argb(0.25f + 0.75f * data.alpha, 1f, 0f, 0f),
                                    3f,
                                    Color.BLACK
                                )
                                val options: PolygonOptions =
                                    PolygonOptions.from(
                                        MapPoints.fromLatLng(areaPoints),
                                        polygonStyle
                                    )

                                val polygon: Polygon =
                                    kakaoMap.shapeManager!!.layer.addPolygon(options)
                                polygons.add(polygon)

                                val body = GuiLayout(Orientation.Horizontal)
                                body.setPadding(20, 20, 20, 18)
                                val bgImage = GuiImage(R.drawable.window_body, true)
                                bgImage.setFixedArea(7, 7, 7, 7)
                                body.setBackground(bgImage)

                                val text = GuiText(
                                    "${
                                        data.addressName.split(" ").last()
                                    } ${DecimalFormat("#,###").format(data.totalOccurCount.toDouble())}"
                                )
                                text.setTextSize(20)
                                body.addView(text)

                                val infoLatLng =
                                    LatLng.from(data.lat.toDouble(), data.lng.toDouble())
                                val infoOptions = InfoWindowOptions.from(infoLatLng)
                                infoOptions.setBody(body)
                                infoOptions.setBodyOffset(0f, -1f)
                                infoOptions.setTail(GuiImage(R.drawable.window_tail, false))

                                val infoWindow =
                                    kakaoMap.mapWidgetManager!!.infoWindowLayer.addInfoWindow(
                                        infoOptions
                                    )
                                infoWindows.add(infoWindow)

                            }
                            lastCallGeometry = null //요청 처리 완료되었으니 비워준다.
                        }

                        override fun onFailure(call: Call<ClusteringResponse>, err: Throwable) {
                            Log.d("RESULT", "통신 오류 발생");
                            Log.d("RESULT", err.toString());
                            lastCallGeometry = null //요청 처리 완료되었으니 비워준다.
                        }
                    })
                }
            }

            override fun onFailure(call: Call<CenterAddrResponse>, t: Throwable) {
                println("Network call failed: ${t.message}")
            }
        })
    }

    fun killPolygons() {
        polygons.forEach { polygon ->
            kakaoMap.shapeManager!!.layer.remove(polygon)
        }
    }

    fun killInfoWindows() {
        infoWindows.forEach { infoWindow ->
            kakaoMap.mapWidgetManager!!.infoWindowLayer.remove(infoWindow)
        }
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
