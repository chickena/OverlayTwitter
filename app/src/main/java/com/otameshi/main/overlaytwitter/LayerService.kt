package com.otameshi.main.overlaytwitter

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.overlay.view.*
import java.util.*

class LayerService : Service() {
    companion object {
        val ACTION_START = "start"
        val ACTION_STOP = "stop"
    }

    private val notificationId = Random().nextInt()

    private var item:FloatingItem? = null
    var videoCustomView: View? = null

    /**
     * ActivityのonCreate()と似たようなもの
     */
    override fun onCreate(){
        super.onCreate()
        startNotification()
    }

    /**
     * 通知を表示しフォアグラウンド処理に移行する
     */
    private fun startNotification(){
        val activityIntent = Intent(this,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,activityIntent,0)
        val notification = Notification.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(LayerService::class.simpleName)
                .setContentText("Service is running.")
                .build()
        startForeground(notificationId,notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null || intent.action == ACTION_START){
            startOverlay()
        }else{
            stopSelf()
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopOverlay()
    }

    /**
     *
     * オーバーレイ表示を開始する
     * この時にWebViewをいろいろしてます。
     * レイアウトもオーバーレイ表示専用のレイアウトを使用
     * (WebViewを置いてあるだけやけどw)
     */
    private fun startOverlay(){
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.overlay,null)
        val setting = view.floatingWebView.settings
        view.floatingWebView.run{
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            settings.javaScriptEnabled = true
            settings.setAppCacheEnabled(true)
            loadUrl("https://twitter.com/?lang=ja")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return false
                }
            }
            item = FloatingItem(windowManager,view.floatingWebView).apply {
                visible = true
            }
        }
    }

    /**
     * オーバーレイ表示を中止する
     */

    private fun stopOverlay(){
        item?.run{
            visible = false
        }
        item = null
    }
}
