package com.hci.chatbot.maps

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.hci.chatbot.network.DiseaseListItemData
import com.hci.chatbot.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetList : BottomSheetDialogFragment() {

    private lateinit var viewOfLayout: View
    private lateinit var addressTextView: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: DiseaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewOfLayout = inflater.inflate(R.layout.disease_list_layout, container, false)

        addressTextView = viewOfLayout.findViewById(R.id.title)

        recyclerView = viewOfLayout.findViewById(R.id.disease_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = DiseaseAdapter(emptyList())
        recyclerView.adapter = adapter

        return viewOfLayout
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        adapter.clearItems()
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun setData(address: String, list: List<DiseaseListItemData>) {
        addressTextView.text = address

        adapter.setItems(list)

        val transition = ChangeBounds()
        transition.duration = 300
        transition.interpolator = FastOutSlowInInterpolator()
        TransitionManager.beginDelayedTransition(viewOfLayout.findViewById<LinearLayout>(R.id.innerContainer), transition)

        val layoutParams = recyclerView.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        recyclerView.layoutParams = layoutParams
        recyclerView.requestFocus()
        recyclerView.bringToFront()

        val bottomSheetView = view?.parent as? ViewGroup
        bottomSheetView?.let {
            TransitionManager.beginDelayedTransition(it, transition)
            val bottomSheetLayoutParams = it.layoutParams
            bottomSheetLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            it.layoutParams = bottomSheetLayoutParams
        }

        TransitionManager.endTransitions(viewOfLayout.findViewById<FrameLayout>(R.id.innerContainer))
    }
}
