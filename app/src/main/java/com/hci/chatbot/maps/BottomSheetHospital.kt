package com.hci.chatbot.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.hci.chatbot.network.DiseaseListItemData
import com.hci.chatbot.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.chatbot.MainActivity
import java.security.Permission


class BottomSheetHospital : BottomSheetDialogFragment() {

    private lateinit var viewOfLayout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewOfLayout = inflater.inflate(R.layout.hospital_info_layout, container, false)

        val phoneNumber = arguments?.getString("phone")

        viewOfLayout.findViewById<TextView>(R.id.hospital_name).text = arguments?.getString("hospital_name")
        viewOfLayout.findViewById<TextView>(R.id.hospital_address).text = arguments?.getString("hospital_address")
        viewOfLayout.findViewById<Button>(R.id.call_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL);
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }

        return viewOfLayout
    }
}
