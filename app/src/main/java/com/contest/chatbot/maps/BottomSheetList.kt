package com.contest.chatbot.maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contest.chatbot.DiseaseListItemData
import com.contest.chatbot.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetList : BottomSheetDialogFragment() {

    private lateinit var viewOfLayout: View
    private lateinit var diseaseList: List<DiseaseListItemData>
    private lateinit var address: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.disease_list_layout, container, false)

        viewOfLayout.findViewById<TextView>(R.id.title).text = buildString {
            append(address)
            append("의 질병 발생 현황")
        };

        val recyclerView = viewOfLayout.findViewById<RecyclerView>(R.id.disease_recycler_view) //최초 초기화만 해준다.
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = DiseaseAdapter(diseaseList) //생성
        return viewOfLayout
    }

    fun setData(address: String, list: List<DiseaseListItemData>) {
        this.diseaseList = list
        this.address = address
    }
}
