package com.peanut.xunleivpn

import android.content.Context
import android.os.Handler
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

object Cache {
    val task = mutableListOf<String>()
    val taskName = mutableStateListOf<String>()
    var callback :((String) -> Unit)? = null
    private val client = OkHttpClient()

    fun add(k:String, v:String){
        println("$k:$v")
        SettingManager[k] = v
    }

    fun get(k:String) = SettingManager.getValue(k, "")

    fun addTask(url:String){
        if(task.indexOf(url) == -1) {
            val matcher = Pattern.compile("https?://.*/download/\\?.*fileid=(.*?)&").matcher(url)
            if (matcher.find()) {
                matcher.group(1)?.let { fid->
                    get(fid).let { fileName->
                        if (fileName.isNotBlank())
                            try {
                                callback?.invoke(fileName)
                                task.add(url)
                                taskName.add(fileName)
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                    }
                }

            }
        }
    }

    fun clearTask(){
        task.clear()
        taskName.clear()
    }

    fun forEachTask(func:(String, String)->Unit){
        val tmp = mutableListOf<Pair<String, String>>()
        for (i in 0 until task.size){
            tmp.add(taskName[i] to task[i])
        }
        tmp.forEach {
            func.invoke(it.first, it.second)
        }
    }

    fun send(url: String, name: String, context: Context){
        try {
            val request: Request = Request.Builder()
                .url(
                    "http://${
                        SettingManager.getValue(
                            "ip",
                            ""
                        )
                    }/remote_download"
                )
                .post(
                    FormBody.Builder()
                        .add("out", name)
                        .add("url", url)
                        .build()
                )
                .build()
            val res = client.newCall(request).execute()
            if (res.code == 200){
                this.taskName.remove(name)
                this.task.remove(url)
            }
            Handler(context.mainLooper).post {
                Toast.makeText(
                    context,
                    name,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(context.mainLooper).post {
                Toast.makeText(
                    context,
                    e.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}