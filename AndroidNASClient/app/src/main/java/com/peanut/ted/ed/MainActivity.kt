package com.peanut.ted.ed

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.peanut.ted.ed.Unities.httpThread
import com.peanut.ted.ed.Unities.resolveUrl
import com.peanut.ted.ed.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var adapter: AlbumAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(ActivityMainBinding.inflate(layoutInflater).also { binding = it }.root)
        SettingManager.init(this)
        binding.refresh.setOnRefreshListener {
            thread {
                try {
                    getJson {
                        val albums = mutableListOf<String>()
                        val titles = JSONObject()
                        for (i in 0 until it.length()) {
                            val data = it.getJSONObject(i)
                            if (data.getString("type") == "Directory")
                                if (data.getString("watched") != "watched" || SettingManager.getValue(
                                        "show_watched",
                                        false
                                    )
                                ) {
                                    titles.put(data.getString("name"), data.getString("title"))
                                    albums.add(data.getString("name"))
                                }
                        }
                        Handler(mainLooper).post {
                            adapter?.changeDataset(albums, titles)
                        }
                    }
                } catch (e: Exception) {
                    Handler(mainLooper).post {
                        Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                } finally {
                    binding.refresh.isRefreshing = false
                }
            }
        }
        refresh()
    }

    private fun refresh() = thread { refreshOnThread() }

    private fun refreshOnThread() {
        try {
            val user = SettingManager.getValue("user", "")
            val ps = SettingManager.getValue("password", "")
            val server = SettingManager.getValue("ip", "192.168.10.208").resolveUrl()
            "$server/userLogin?name=${Uri.encode(user)}&psw=${Uri.encode(ps)}".httpThread{}
            getJson {
                val albums = mutableListOf<String>()
                val titles = JSONObject()
                for (i in 0 until it.length()) {
                    val data = it.getJSONObject(i)
                    if (data.getString("type") == "Directory")
                        if (data.getString("watched") != "watched" || SettingManager.getValue("show_watched", false)) {
                            titles.put(data.getString("name"), data.getString("title"))
                            albums.add(data.getString("name"))
                        }
                }
                Handler(mainLooper).post {
                    binding.rv.adapter = AlbumAdapter(
                        this,this,
                        albums = albums, titles = titles).also { adapter -> this.adapter = adapter }
                    binding.rv.layoutManager = StaggeredGridLayoutManager(
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 5,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                }
            }
        } catch (e: Exception) {
            Handler(mainLooper).post {
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getJson(func: (JSONArray) -> Unit) {
        val server = SettingManager.getValue("ip", "192.168.10.208").resolveUrl()
        "$server/getFileList?path=/".httpThread{ body ->
            func.invoke(JSONArray(body ?: "[]"))
        }
    }
}