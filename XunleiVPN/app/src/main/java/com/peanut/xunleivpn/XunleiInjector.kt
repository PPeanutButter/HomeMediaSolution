package com.peanut.xunleivpn

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.github.megatronking.netbare.http.*
import com.github.megatronking.netbare.injector.InjectorCallback
import com.github.megatronking.netbare.injector.SimpleHttpInjector
import org.json.JSONObject
import java.io.EOFException
import java.io.InputStreamReader
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream


class XunleiInjector(private val context: Context) : SimpleHttpInjector() {
    private var byteArrayCache:ByteArray = ByteArray(0)
    private var cacheId:String = ""

    override fun sniffResponse(response: HttpResponse) =
        response.url().startsWith("https://api-pan.xunlei.com/drive/v1/files",true)

    override fun sniffRequest(request: HttpRequest) =
        Pattern.compile("https?://.*/download/\\?.*fileid=(.*?)&").matcher(request.url()).find().also {
            if (it) {
                println("action.xunlei.download: ${request.url()}")
                Cache.addTask(request.url())
            }
        }

    /**
     * 下拉刷新得到的结果
     */
    private fun list(jsonObject: JSONObject){
        val files = jsonObject.getJSONArray("files")
        for (i in 0 until files.length())
            files.getJSONObject(i).let {
                Cache.add(it.getString("id"), it.getString("name"))
            }
        Handler(context.mainLooper).post {
            Toast.makeText(
                context,
                "XunleiInjector: 已缓存${files.length()}个文件名",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResponseInject(response: HttpResponse, body: HttpBody, callback: InjectorCallback) {
        try {
            if (response.id() != cacheId){
                cacheId = response.id()
                byteArrayCache = ByteArray(0)
            }
            byteArrayCache = byteArrayCache.plus(body.toBuffer().array().also { println("该段:${it.size}") })
            val reader = InputStreamReader(GZIPInputStream(byteArrayCache.also { println("最终合并后:${it.size}") }.inputStream()))
            val sb = StringBuffer()
            for(line in reader.readLines()) sb.append(line)
            val json = JSONObject(sb.toString().also { println(it) })
            if (json.has("files"))
                list(json)
        }catch (eof:EOFException){
            println("数据不完整, 等待下一段数据")
        }catch (e:Exception){
            e.printStackTrace()
            Handler(context.mainLooper).post {
                Toast.makeText(context, "XunleiInjector: "+e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        callback.onFinished(body)
    }
}