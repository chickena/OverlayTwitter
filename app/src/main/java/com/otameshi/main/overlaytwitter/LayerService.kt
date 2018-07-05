package com.otameshi.main.overlaytwitter

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
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
        val ACTION_RESTART = "restart"
        val ACTION_SUSPEND = "suspend"
    }

    private val notificationId = Random().nextInt()
    private val channelId = "LayerService_notification_channel"
    val notifyDescription = "OverlayTwitter"
    val name = "Twitter"
    private var item:FloatingItem? = null
    var videoCustomView: View? = null

    /**
     * ActivityのonCreate()と似たようなもの
     */
    override fun onCreate(){
        super.onCreate()
        lateinit var channel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channel = NotificationChannel(channelId,name,importance)
            manager.createNotificationChannel(channel)
            if (manager.getNotificationChannel(channelId) == null) {
                val mChannel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
                mChannel.apply {
                    description = notifyDescription
                }
                manager.createNotificationChannel(mChannel)
            }
        }
        startNotification()
    }

    /**
     * 通知を表示しフォアグラウンド処理に移行する
     */
    private fun startNotification(){

        val suspendIntent = Intent(this,LayerService::class.java).setAction(LayerService.ACTION_SUSPEND)
        val restartIntent = Intent(this,LayerService::class.java).setAction(LayerService.ACTION_RESTART)
        val stopIntent =  Intent(this,LayerService::class.java).setAction(LayerService.ACTION_STOP)
        val pendingSuspendIntent = PendingIntent.getService(this,0,suspendIntent,0)
        val pendingRestartIntent = PendingIntent.getService(this,0,restartIntent,0)
        val pendingStopIntent = PendingIntent.getService(this,0,stopIntent,0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = NotificationCompat.Builder(this, channelId)
                    .apply{
                        setContentTitle(LayerService::class.simpleName)
                        setSmallIcon(R.drawable.notification_icon_background)
                        addAction(1, "一時停止", pendingSuspendIntent)
                        addAction(2, "再開", pendingRestartIntent)
                        addAction(3, "終了", pendingStopIntent)
                    }

            startForeground(notificationId, builder.build())
        }else {
            val notification = Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(LayerService::class.simpleName)
                    .addAction(1, "一時停止", pendingSuspendIntent)
                    .addAction(2, "再開", pendingRestartIntent)
                    .addAction(3, "終了", pendingStopIntent)
                    .build()
            startForeground(notificationId, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null || intent.action == ACTION_START){
            startOverlay()
        }else if(intent == null || intent.action == ACTION_SUSPEND){
//            stopSelf()
            suspendOverlay()
        }else if(intent == null || intent.action == ACTION_RESTART){
            restartOverlay()
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
            //ここで呼ぶ？
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

    private fun suspendOverlay(){
        item?.run{
            view.floatingWebView.visibility = View.GONE
        }
    }
    private fun stopOverlay(){
        item?.run{
            visible = false
        }
        item = null
    }
    private  fun restartOverlay(){
        item?.run{
            view.floatingWebView.visibility = View.VISIBLE
        }
    }
}
