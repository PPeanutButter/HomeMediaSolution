package com.peanut.ted.ed

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.peanut.ted.ed.Unities.calculateColorLightValue
import com.peanut.ted.ed.Unities.httpThread
import com.peanut.ted.ed.Unities.name
import com.peanut.ted.ed.Unities.resolveUrl
import com.peanut.ted.ed.Unities.round
import com.peanut.ted.ed.databinding.ActivityDetailBinding
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var album: String
    private val launchTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        album = (intent.getStringExtra("ALBUM")?:"错误").also {
            binding.toolbarLayout.title = " "
        }
        binding.cover.round(10.dp)
        setSupportActionBar(binding.toolbar)
        refresh()
    }

    private fun refresh() = thread { refreshOnThread() }

    @SuppressLint("SetTextI18n")
    private fun refreshOnThread() {
        try {
            getJson(album) {
                val images = ArrayList<String>()
                val titles = ArrayList<String>()
                val attaches = ArrayList<String>()
                val times = ArrayList<String>()
                val server = SettingManager.getValue("ip", "").resolveUrl()
                for (index in 0 until it.length()) {
                    val jsonObject = it.getJSONObject(index)
                    val episode = jsonObject.getString("name")
                    if (jsonObject.getString("type") == "Attach")
                        attaches.add(episode)
                    else if (jsonObject.getString("watched") != "watched" || SettingManager.getValue("show_watched", false)) {
                        titles.add(episode.name())
                        images.add("$server/getVideoPreview?path=${Uri.encode(episode)}&" +
                                "token=${SettingManager.getValue("token", "")}")
                        times.add(
                            (if (jsonObject.getString("bitrate") != "") jsonObject.getString("bitrate")
                            else Unities.getFileLengthDesc(jsonObject.getLong("length"))) + "  " + jsonObject.getString(
                                "desc"
                            )
                        )
                    }
                }
                Handler(mainLooper).post {
                    Picasso.get().load(
                        "$server/getFile/get_post_img?" +
                                "path=${Uri.encode("/$album/.post")}&" +
                                "token=${SettingManager.getValue("token", "")}"
                    ).error(R.mipmap.post).also { requestCreator ->
                            thread {
                                Palette.from(requestCreator.get()).generate { palette ->
                                    // Use generated instance
                                    val vibrantBody = (palette?.dominantSwatch?.rgb)
                                        ?: Color.parseColor("#7367EF")
                                    binding.toolbarLayout.setContentScrimColor(vibrantBody)
                                    binding.toolbarLayout.setBackgroundColor(vibrantBody)
                                    binding.toolbarLayout.setStatusBarScrimColor(vibrantBody)
                                    val light = calculateColorLightValue(vibrantBody)
                                    val color = if (light < 0.4) Color.WHITE else Color.BLACK
                                    binding.textView.setTextColor(color)
                                    binding.textView2.setTextColor(color)
                                    binding.textView3.setTextColor(color)
                                    binding.textView9.setTextColor(color)
                                    val controller = ViewCompat.getWindowInsetsController(binding.root)
                                    controller?.isAppearanceLightStatusBars = light >= 0.4
                                }
                            }
                        }.into(binding.post).also {
                            binding.post.visibility = View.VISIBLE
                        }
                    Picasso.get().load(
                        "$server/getCover?cover=${Uri.encode(album)}&" +
                                "token=${SettingManager.getValue("token", "")}"
                    ).error(R.mipmap.cover)
                        .placeholder(R.mipmap.cover).into(binding.cover)
                }
                if (System.currentTimeMillis() - launchTime < 500){
                    //等待activity动画结束再加载数据防止动画卡顿
                    Thread.sleep(500 - System.currentTimeMillis() + launchTime)
                }
                Handler(mainLooper).post {
                    val rv = findViewById<RecyclerView>(R.id.rv)
                    rv.adapter = EpisodeAdapter(
                        this,
                        titles = titles,
                        images = images,
                        dates = times,
                        album = album
                    )
                    rv.layoutManager = StaggeredGridLayoutManager(
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                    //attach
                    val attach = findViewById<RecyclerView>(R.id.attachment)
                    attach.adapter = AttachAdapter(
                        this,
                        titles = attaches
                    )
                    attach.layoutManager = StaggeredGridLayoutManager(
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 2,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                }
            }
            getInfo {
                Handler(mainLooper).post {
                    binding.textView.text = it.getString("title")
                    binding.textView2.text =
                        "${it.getString("certification")} ${it.getString("genres")} • ${it.getString("runtime")}"
                    binding.include2.textView6.text = it.getInt("user_score_chart").toString()
                    binding.include2.circularProgressView.progress = it.getInt("user_score_chart")
                    binding.textView3.text = "“${it.getString("tagline")}”"
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Handler(mainLooper).post {
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun getJson(album:String, func: (JSONArray) -> Unit) {
        val server = SettingManager.getValue("ip", "192.168.10.208").resolveUrl()
        "$server/getFileList?path=/$album/".httpThread{ body ->
            thread { func.invoke(JSONArray(body ?: "[]")) }
        }
    }

    private fun getInfo(func: (JSONObject) -> Unit) {
        val server = SettingManager.getValue("ip", "192.168.10.208").resolveUrl()
        "$server/getFile/get_album_info?path=${Uri.encode("/$album/.info")}".httpThread { body ->
            thread { func.invoke(JSONObject(body ?: "{}")) }
        }
    }

    inline val Double.dp: Float get() = run {
        return toFloat().dp
    }
    inline val Int.dp: Float get() = run {
        return toFloat().dp
    }
    inline val Float.dp: Float get() = run {
        val scale: Float = this@DetailActivity.resources.displayMetrics.density
        return (this * scale + 0.5f)
    }
}