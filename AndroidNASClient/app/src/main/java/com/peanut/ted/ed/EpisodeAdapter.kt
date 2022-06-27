package com.peanut.ted.ed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.Unities.copy
import com.peanut.ted.ed.Unities.http
import com.peanut.ted.ed.Unities.play
import com.peanut.ted.ed.Unities.resolveUrl
import com.peanut.ted.ed.Unities.toast
import com.squareup.picasso.Picasso

class EpisodeAdapter(
        private val context: Context,
        private val titles: MutableList<String>,
        private val images: MutableList<String>,
        private val dates: MutableList<String>,
        private val album: String
) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MyViewHolder(LayoutInflater.from(context).inflate(R.layout.card, parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tv.text = titles[position]
        holder.date.text = dates[position]
        loadImg(images[position], holder.iv)
        val server = SettingManager.getValue("ip", "192.168.1.101:80").resolveUrl()
        holder.actionPlay.setOnClickListener {
            ("$server/getFile/${titles[position]}?" +
                    "path=${Uri.encode("/"+album+"/"+titles[position])}&" +
                    "token=${SettingManager.getValue("token", "")}")
                .play(this@EpisodeAdapter.context)
        }
        holder.actionLink.setOnClickListener {
            val link = "$server/getFile/${titles[position]}?" +
                    "path=${Uri.encode("/"+album+"/"+titles[position])}&" +
                    "token=${SettingManager.getValue("token", "")}"
            link.copy(this@EpisodeAdapter.context)
            "已复制链接".toast(this.context)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(link), "video/mp4")
            context.startActivity(intent)
        }
        holder.actionBook.setOnClickListener {
            "$server/toggleBookmark?path=${Uri.encode("/"+album+"/"+titles[position])}".http(this@EpisodeAdapter.context) { _ ->
                titles.removeAt(position)
                images.removeAt(position)
                dates.removeAt(position)
                notifyItemRemoved(position)
                if (position != itemCount) {
                    notifyItemRangeChanged(position, itemCount)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    private fun loadImg(url: String,iv: ImageView) {
        Picasso.get().load(url).priority(Picasso.Priority.HIGH).error(R.mipmap.preview)
            .placeholder(R.mipmap.preview).into(iv)
    }
}