package com.hci.chatbot.maps

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hci.chatbot.network.DiseaseListItemData
import com.hci.chatbot.R
import java.text.SimpleDateFormat
import java.util.Locale

class DiseaseAdapter(private var items: List<DiseaseListItemData>) : RecyclerView.Adapter<DiseaseAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val location: TextView = itemView.findViewById(R.id.detail_location)
        val diseaseType: TextView = itemView.findViewById(R.id.type_disease)
        val occurDate: TextView = itemView.findViewById(R.id.when_occurs)
        val occurCount: TextView = itemView.findViewById(R.id.occur_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.disease_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.location.text = item.addressName
        holder.diseaseType.text = buildString {
            append(item.livestockType)
            append(item.diseaseName)
        }
        holder.occurDate.text = SimpleDateFormat("발생일: yyyy년 MM월 dd일", Locale.KOREAN).format(item.occurDate)
        holder.occurCount.text = item.occurCount.toString()
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun clearItems() {
        items = emptyList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<DiseaseListItemData>) {
        this.items = items
        notifyDataSetChanged()
    }
}
