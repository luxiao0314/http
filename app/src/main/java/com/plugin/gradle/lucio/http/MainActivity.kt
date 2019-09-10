package com.plugin.gradle.lucio.http

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.ifog.timedebug.TimeDebugerManager
import com.plugin.gradle.lucio.core.http.DownloadListener
import com.plugin.gradle.lucio.core.http.Request
import com.plugin.gradle.lucio.core.http.STSResponse
import com.plugin.gradle.lucio.core.http.SimpleResponseListener
import java.io.File


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 自定义自己喜欢的log输出方式
        TimeDebugerManager.setLogger { method, cost ->
            if (cost > 10) {
                Log.e("=======AntiTime=======", "#$method:cost:$cost")
            }
        }
    }

    fun click(v: View?) {
        init()
    }

    fun download(v: View?) {
//        Request.get<File>("http://oss.cdn.aiclk.com/live/app/1576673/596e62f75c05a7fd96344cebf747a2be_1576673.apk")
//            .enqueue(object : DownloadListener<File>(getStoragePath(this)?.absolutePath, "cnblogs111.apk") {
//                override fun onSuccess(content: File?) {
//                    Log.d("onSuccess", content?.absolutePath)
//                }
//            })
        Request.get<File>("https://mtabc.aihuishou.com/eraser/apk/20190716173913234490695.json")
            .enqueue(object : DownloadListener<File>(getStoragePath(this)?.absolutePath, "contract.json") {
                override fun onSuccess(content: File?) {
                    Log.d("onSuccess", content?.absolutePath)
                }
            })
    }

    fun upload(v: View?) {
        Request.get<String>("http://47.96.53.33:8080/creative-jdy/bjs/api/v1.0/external/appVersion/uploadTest")
            .fileParam("aaaaa", File(getStoragePath(this)?.absolutePath + "/" + "cnblogs111.apk"))
            .enqueue(object : SimpleResponseListener<String>() {
                override fun onSuccess(content: String?) {
                    Log.e("String", content)
                }
            })
    }

    private fun init() {
        Request.get<STSResponse>("http://47.96.53.33:8080/creative-jdy/fe/oss/token")
            .enqueue(object : SimpleResponseListener<STSResponse>() {
                override fun onSuccess(content: STSResponse?) {
                    Log.e("String", content?.accessKeySecret)
                }
            })

//        Request.get<Response>("http://v2.api.dmzj.com/novel/recommend.json")
//            .call(CusCall())
//            .enqueue(object : SimpleResponseListener<Response>() {
//                override fun onSuccess(content: Response?) {
//
//                }
//            })
//
//        async.get("http://v2.api.dmzj.com/novel/recommend.json") {
//            Log.d("response", this.text)
//        }
//

//        Request.get<File>("http://oss.cdn.aiclk.com/live/app/1576673/596e62f75c05a7fd96344cebf747a2be_1576673.apk")
//            .call(CusCall())
//            .enqueue(object : DownloadListener<File>(getStoragePath(this).absolutePath, "cnblogs222.apk") {
//                override fun onSuccess(content: File?) {
//                    Log.d("onSuccess", content?.absolutePath)
//                }
//            })

//        UrlHttpUtil.downloadFile("http://oss.cdn.aiclk.com/live/app/1576673/596e62f75c05a7fd96344cebf747a2be_1576673.apk",
//            object : CallBackUtil.CallBackFile(getStoragePath(this).absolutePath, "cnblogs.apk") {
//                override fun onFailure(code: Int, errorMessage: String) {
//                    Log.d("onFailure", errorMessage)
//                }
//
//                override fun onResponse(response: File) {
//                    Log.d("onResponse", response.absolutePath)
//                }
//
//                override fun onProgress(progress: Float, total: Long) {
//                    Log.d("onProgress", progress.toString())
//                }
//            })

//        Request.get<Response>("http://horus.aiclk.com/v2/report")
//            .param("aa",File("aa"))
//            .enqueue(object : SimpleResponseListener<Response>() {
//                override fun onSuccess(content: Response?) {
//
//                }
//            })
    }

    fun getStoragePath(context: Context): File? {
        var storagePath = context.getExternalFilesDir("aiclk_down")
        if (storagePath == null) {
            storagePath = Environment.getExternalStorageDirectory()
        }
        return storagePath
    }
}
