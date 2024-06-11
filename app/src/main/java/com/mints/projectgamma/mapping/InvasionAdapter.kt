package com.mints.projectgamma.mapping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mints.projectgamma.R
import com.mints.projectgamma.api.Invasion

//class InvasionAdapter(private val invasions: List<Invasion>) : RecyclerView.Adapter<InvasionAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val nameTextView: TextView = view.findViewById(R.id.text_view_name)
//        val locationTextView: TextView = view.findViewById(R.id.text_view_location)
//        // Add other TextViews for additional data
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invasion, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val invasion = invasions[position]
//        holder.nameTextView.text = invasion.name
//        holder.locationTextView.text = "Location: <a href=\"https://ipogo.app/?coords=${invasion.lat},${invasion.lng}\">Teleport</a>"
//        // Bind other data
//    }
//
//    override fun getItemCount() = invasions.size
//}
