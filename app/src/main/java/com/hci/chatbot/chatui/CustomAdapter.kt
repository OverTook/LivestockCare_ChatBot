package com.hci.chatbot.chatui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hci.chatbot.R

class CustomAdapter(private val myDataList: ArrayList<Item>, private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun addItem(item: Item) {
        myDataList.add(item)
        this.notifyItemInserted(myDataList.size - 1)
        recyclerView.smoothScrollToPosition(myDataList.size - 1)
    }

    fun removeLastAndAddItem(item: Item) {
        myDataList.removeAt(myDataList.size - 1)
        myDataList.add(item)
        this.notifyItemChanged(myDataList.size - 1)
        recyclerView.smoothScrollToPosition(myDataList.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearItems() {
        myDataList.clear()
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return when (viewType) {
            ViewType.WAIT_MSG -> {
                view = inflater.inflate(R.layout.chat_wait, parent, false)
                WaitViewHolder(view)
            }
            ViewType.LEFT_CHAT -> {
                view = inflater.inflate(R.layout.chat_left, parent, false)
                LeftViewHolder(view)
            }
            else -> {
                view = inflater.inflate(R.layout.chat_right, parent, false)
                RightViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val item = myDataList[position]
        if(!item.visible) {
            viewHolder.itemView.visibility = View.GONE
        } else {
            viewHolder.itemView.visibility = View.VISIBLE
        }

        when (viewHolder) {
            is LeftViewHolder -> {
                viewHolder.content.text = item.content
                viewHolder.time.text = item.time
            }
            is RightViewHolder -> {
                viewHolder.content.text = item.content
                viewHolder.time.text = item.time
            }
        }
    }

    override fun getItemCount(): Int {
        return myDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return myDataList[position].viewType
    }

    class WaitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    class LeftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.chat_receive_content)
        val time: TextView = itemView.findViewById(R.id.chat_receive_time)
    }

    class RightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.chat_send_content)
        val time: TextView = itemView.findViewById(R.id.chat_send_time)
    }
}

