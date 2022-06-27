package com.peanut.ted.ed

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.Unities.gone
import com.peanut.ted.ed.Unities.name
import java.util.regex.Pattern
import com.peanut.ted.ed.Unities.resolveUrl
import com.squareup.picasso.Picasso
import org.json.JSONObject

/**
 * 显示海报墙
 */
class AlbumAdapter(
        private val context: Context,
        private val activity: Activity,
        private var albums: MutableList<String>,
        private var titles: JSONObject
) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MyViewHolder(LayoutInflater.from(context).inflate(R.layout.album, parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Pattern.compile("(.*)\\(\\d{4}\\)", Pattern.MULTILINE).matcher(titles.getString(albums[position])).apply { this.find() }.also {
            if (it.groupCount() == 1)
                holder.tv.text = it.group(1)
            else holder.tv.text = albums[position].name()
        }
        val server = SettingManager.getValue("ip", "192.168.10.208").resolveUrl()
        loadImg("$server/getCover?cover=${Uri.encode(albums[position])}&" +
                "token=${SettingManager.getValue("token", "")}", holder.iv)
        holder.date.gone()
        holder.actionPlay.gone()
        holder.actionLink.gone()
        holder.actionBook.gone()
        holder.card.setOnClickListener {
            context.startActivity(Intent(context, DetailActivity::class.java).putExtra("ALBUM", albums[position]), ActivityOptions.makeSceneTransitionAnimation(
                activity, holder.iv, "ted-cover").toBundle())
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    private fun loadImg(url: String,iv: ImageView) {
        Picasso.get().load(url).priority(Picasso.Priority.HIGH).error(R.mipmap.cover)
            .placeholder(R.mipmap.cover).into(iv)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeDataset(albums: MutableList<String>, titles: JSONObject){
        this.albums = albums
        this.titles = titles
        this.notifyDataSetChanged()
    }
}