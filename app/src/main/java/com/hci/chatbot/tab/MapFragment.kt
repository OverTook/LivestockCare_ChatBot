package com.hci.chatbot.tab

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.hci.chatbot.MainActivity
import com.hci.chatbot.R
import com.hci.chatbot.maps.BottomSheetHospital
import com.hci.chatbot.maps.BottomSheetList
import com.hci.chatbot.network.ClusteringResponse
import com.hci.chatbot.network.DiseaseListResponse
import com.hci.chatbot.network.Hospital
import com.hci.chatbot.network.HospitalResponse
import com.hci.chatbot.network.NetworkManager
import com.hci.chatbot.utils.SharedPreferenceManager
import com.hci.chatbot.utils.TutorialUtil
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.Poi
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.mapwidget.InfoWindow
import com.kakao.vectormap.mapwidget.InfoWindowOptions
import com.kakao.vectormap.mapwidget.component.GuiImage
import com.kakao.vectormap.mapwidget.component.GuiLayout
import com.kakao.vectormap.mapwidget.component.GuiText
import com.kakao.vectormap.mapwidget.component.Orientation
import com.skydoves.powerspinner.PowerSpinnerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors


class MapFragment : Fragment() {

    private val tutorialUtil = TutorialUtil.getInstance()

    private lateinit var viewOfLayout: View
    private lateinit var mapView: MapView
    private lateinit var kakaoMap: KakaoMap
    private lateinit var supportFragmentManager: FragmentManager
    private lateinit var diseaseListManager: BottomSheetList
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var currentLocationMarker: Label

    private lateinit var filterAnimal: PowerSpinnerView
    private lateinit var filterOption: PowerSpinnerView

    private val executor = Executors.newSingleThreadScheduledExecutor()

    private val infoWindows = mutableListOf<InfoWindow>()
    private val hospitalLabels = mutableListOf<Label>()

    //질병 목록 조회, 다른 목록 요청시 취소하기 위해 전역 변수로 캐싱
    private var lastCallDisease: Call<DiseaseListResponse>? = null
    private var lastCallClustering: Call<ClusteringResponse>? = null
    private var lastCallHospital: Call<HospitalResponse>? = null
    private var toast: Toast? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewOfLayout = inflater.inflate(R.layout.fragment_map, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.Builder(5000)
            .setIntervalMillis(5000)
            .setMinUpdateIntervalMillis(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        sharedPreferenceManager = SharedPreferenceManager(requireActivity())
        if(sharedPreferenceManager.getFirstCheckMap()) {
            tutorialUtil.mapTutorialing = true
        }

        filterAnimal = viewOfLayout.findViewById(R.id.filter_animal)
        filterOption = viewOfLayout.findViewById(R.id.filter_option)
        filterOption.setOnSpinnerItemSelectedListener { oldIndex: Int, _: Any?, newIndex: Int, _: Any ->
            if (oldIndex != 2 && newIndex == 2) {
                filterAnimal.visibility = View.INVISIBLE
            } else if(oldIndex == 2 && newIndex != 2) {
                filterAnimal.visibility = View.VISIBLE
            }

            getMarkers()
            if(newIndex == 1) {
                deleteAllHospital()
            } else if(newIndex == 2) {
                deleteAllMarker()
            }
        }

        mapView = viewOfLayout.findViewById(R.id.kakaoMap)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }
            override fun onMapError(error: Exception) {
            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMapReady: KakaoMap) {
                kakaoMap = kakaoMapReady
                kakaoMap.logo!!.setPosition(0, 15f, mapView.height.toFloat() - 50);

                if(!tutorialUtil.mapTutorialing) {
                    getCurrentLocation()
                }

                kakaoMap.setOnMapClickListener { _, pos, _, _ ->
                    if(tutorialUtil.mapTutorialing) return@setOnMapClickListener
                    showDisease(pos)
                }

                kakaoMap.setOnLabelClickListener { _, _, label ->
                    if(label?.tag == null) return@setOnLabelClickListener
                    if(label.tag !is Hospital) return@setOnLabelClickListener

                    val item = (label.tag as Hospital)

                    val bottomSheetFragment = BottomSheetHospital()
                    val arg = Bundle()
                    arg.putString("hospital_name", item.hospitalName)
                    arg.putString("hospital_address", item.address)
                    arg.putString("phone", item.phone)

                    bottomSheetFragment.arguments = arg
                    bottomSheetFragment.show((activity as MainActivity).supportFragmentManager, "bottomSheetHospital")
                }

                var zoomLevel = kakaoMap.zoomLevel
                //카메라 이동이 있을 떄 지도이동이 끝난 후  카메라 값을 가져옴 (이벤트 등록)
                kakaoMap.setOnCameraMoveEndListener { _, _, _ ->
                    Log.e("KakaoZoomLevel", kakaoMap.zoomLevel.toString() + " " + zoomLevel.toString())
                    if(kakaoMap.zoomLevel < 14 && zoomLevel >= 14 && filterOption.selectedIndex != 1) {
                        Toast.makeText(requireContext(), "동물 병원이 표시되지 않는 거리입니다.", Toast.LENGTH_SHORT).show()
                        deleteAllHospital()
                        lastCallHospital?.cancel()
                    }
                    zoomLevel = kakaoMap.zoomLevel

                    getMarkers()
                }
            }
        })

        viewOfLayout.findViewById<ImageButton>(R.id.gps_move_to_current_btn).setOnClickListener {
            if (!this::currentLocationMarker.isInitialized) {
                return@setOnClickListener
            }

            val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                LatLng.from(currentLocationMarker.position.latitude, currentLocationMarker.position.longitude)
            )
            kakaoMap.moveCamera(cameraUpdate, CameraAnimation.from(200, false, false));
        }

        supportFragmentManager = (activity as MainActivity).supportFragmentManager
        diseaseListManager = BottomSheetList()

        return viewOfLayout
    }

    fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.requestLocationUpdates(locationRequest, executor, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        locationResult.let {
                            val lastLocation = locationResult.lastLocation
                            lastLocation?.let {
                                if(this@MapFragment::currentLocationMarker.isInitialized) {
                                    currentLocationMarker.moveTo(LatLng.from(it.latitude, it.longitude))
                                }
                                return
                            }
                        }

                        Snackbar.make(viewOfLayout.findViewById(R.id.main), "현재 위치를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show()
                    }
                })

                fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                    location?.let {
                        val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                            LatLng.from(it.latitude, it.longitude)
                        )


                        val styles = kakaoMap.labelManager!!.addLabelStyles(LabelStyles.from(
                            LabelStyle.from(R.drawable.current_location_marker)))
                        val layer = kakaoMap.labelManager!!.layer

                        val options = LabelOptions.from(LatLng.from(it.latitude, it.longitude)).setStyles(styles)

                        currentLocationMarker = layer!!.addLabel(options)

                        kakaoMap.moveCamera(cameraUpdate, CameraAnimation.from(100, true, false))

                        getMarkers()

                        return@addOnSuccessListener
                    }

                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "현재 위치를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show()
                    getMarkers()
                }
            }
        } catch (e: SecurityException) {
            Log.e("GPS Error", "Security Exception: ${e.message}")
            Snackbar.make(viewOfLayout.findViewById(R.id.main), "현재 위치를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show()
            getMarkers()
        }
    }

    fun showDisease(pos: LatLng) {
        lastCallDisease?.cancel() //이전 요청은 취소한다.

        if(filterOption.selectedIndex != 2) {
//diseaseListManager.setData("지역을 불러오는 중입니다.", ArrayList<DiseaseListItemData>())
            diseaseListManager.show(supportFragmentManager, diseaseListManager.tag)

            lastCallDisease = NetworkManager.apiService.getDiseaseData(
                pos.latitude,
                pos.longitude,
                kakaoMap.zoomLevel,
                when(filterAnimal.selectedIndex) {
                    0 -> "all"
                    1 -> "chicken"
                    2 -> "cow"
                    3 -> "pig"
                    else -> "all"
                })
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
    }

    fun getMarkers() {
        if(tutorialUtil.mapTutorialing) return

        val rightTop = kakaoMap.fromScreenPoint(mapView.width, 0)!!    //제일 높음
        val leftBottom = kakaoMap.fromScreenPoint(0, mapView.height)!! //제일 낮음

        if(filterOption.selectedIndex != 2) {
            lastCallClustering?.cancel()
            lastCallClustering = NetworkManager.apiService.getClusteredData(
                leftBottom.latitude,
                leftBottom.longitude,
                rightTop.latitude,
                rightTop.longitude,
                kakaoMap.zoomLevel,
                when(filterAnimal.selectedIndex) {
                    0 -> "all"
                    1 -> "chicken"
                    2 -> "cow"
                    3 -> "pig"
                    else -> "all"
                })
            lastCallClustering?.enqueue(object : Callback<ClusteringResponse> {
                override fun onResponse(call: Call<ClusteringResponse>, response: Response<ClusteringResponse>) {
                    if(call.isCanceled) return
                    if(!response.isSuccessful) return

                    val prevLabelCount = infoWindows.size

                    val layer = kakaoMap.mapWidgetManager!!.infoWindowLayer

                    response.body()!!.data.forEach { item ->
                        Log.e("ClusteredMarker", item.addressName + " " + item.totalOccurCount)
                        val pos = LatLng.from(item.lat.toDouble(), item.lng.toDouble())


                        val body = GuiLayout(Orientation.Horizontal)
                        body.setPadding(20, 20, 20, 18)

                        val bgImage = GuiImage(R.drawable.window_body, true)
                        bgImage.setFixedArea(7, 7, 7, 7) // 말풍선 이미지 각 모서리의 둥근 부분만큼(7px)은 늘어나지 않도록 고정.
                        body.setBackground(bgImage)

                        //val text = GuiText("InfoWindow!")
                        val text = GuiText(
                            "${
                                item.addressName.split(" ").last()
                            } ${item.totalOccurCount}"
                        )
                        text.setTextSize(30)
                        body.addView(text)

                        var options = InfoWindowOptions.from(pos)
                        options.setBody(body)
                        options.setBodyOffset(0f, -4f) // Body 와 겹치게 -4px 만큼 올려줌.
                        options = options.setTail(GuiImage(R.drawable.window_tail, false))

                        infoWindows.add(layer.addInfoWindow(options))
                    }

                    //이전에 생성된 부분만 제거
                    val subList = infoWindows.subList(0, prevLabelCount)
                    subList.forEach { infoWindow ->
                        layer.remove(infoWindow)
                    }
                    subList.clear()
                    lastCallClustering = null
                }

                override fun onFailure(call: Call<ClusteringResponse>, err: Throwable) {
                    Log.e("GetClusteredData Failed", "Network call failed: ${err.message}")
                    lastCallClustering = null
                }
            })
        }

        if(filterOption.selectedIndex != 1 && kakaoMap.zoomLevel >= 14) {
            lastCallHospital?.cancel()
            lastCallHospital = NetworkManager.apiService.getHospital(leftBottom.latitude, leftBottom.longitude, rightTop.latitude, rightTop.longitude)
            lastCallHospital?.enqueue(object : Callback<HospitalResponse> {
                override fun onResponse(call: Call<HospitalResponse>, response: Response<HospitalResponse>) {
                    if(call.isCanceled) return
                    if (!response.isSuccessful) return

                    val prevLabelCount = hospitalLabels.size

                    val layer = kakaoMap.labelManager!!.layer!!

                    response.body()!!.hospitals.forEach { item ->
                        val styles = kakaoMap.labelManager!!.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.hospital_marker_128px)))
                        val options = LabelOptions.from(LatLng.from(item.latitude, item.longitude)).setStyles(styles)

                        val label = layer.addLabel(options)
                        label.tag = item

                        hospitalLabels.add(label)
                    }

                    //이전에 생성된 부분만 제거
                    val subList = hospitalLabels.subList(0, prevLabelCount)
                    subList.forEach { label ->
                        layer.remove(label)
                    }
                    subList.clear()
                    lastCallHospital = null
                }

                override fun onFailure(call: Call<HospitalResponse>, err: Throwable) {
                    Log.e("GetHospital Failed", "Network call failed: ${err.message}")
                    lastCallHospital = null
                }
            })
        }
    }

    fun deleteAllMarker(){
        val layer = kakaoMap.mapWidgetManager!!.infoWindowLayer
        infoWindows.forEach { item ->
            layer.remove(item)
        }
        infoWindows.clear()
    }

    fun deleteAllHospital(){
        val layer = kakaoMap.labelManager!!.layer!!
        hospitalLabels.forEach { item ->
            layer.remove(item)
        }
        hospitalLabels.clear()
    }

    override fun onResume() {
        super.onResume()
        if(mapView == null) {

        }
        mapView.resume() // MapView 의 resume 호출
    }

    override fun onPause() {
        super.onPause()
        mapView.pause() // MapView 의 pause 호출
    }
}
