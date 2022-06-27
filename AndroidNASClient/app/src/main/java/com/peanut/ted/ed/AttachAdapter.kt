package com.peanut.ted.ed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.Unities.name
import com.peanut.ted.ed.Unities.resolveUrl
import java.net.URLEncoder

class AttachAdapter(
        private val context: Context,
        private val titles: MutableList<String>
) : RecyclerView.Adapter<AttachViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AttachViewHolder(LayoutInflater.from(context).inflate(R.layout.attach, parent, false))

    override fun onBindViewHolder(holder: AttachViewHolder, position: Int) {
        holder.title.text = titles[position].name()
        val server = SettingManager.getValue("ip", "192.168.1.101:80").resolveUrl()
        holder.download.setOnClickListener {
            val link = "$server/getFile/${titles[position].name()}?" +
                    "path=${Uri.encode("/"+titles[position])}&" +
                    "token=${SettingManager.getValue("token", "")}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(link), "video/mp4")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}

class AttachViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.title)
    val download: ImageView = itemView.findViewById(R.id.action_save)
}