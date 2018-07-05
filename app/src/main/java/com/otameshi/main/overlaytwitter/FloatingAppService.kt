//package d.team.ecc.nicotube.floating
//
//import android.annotation.SuppressLint
//import android.annotation.TargetApi
//import android.app.*
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.os.IBinder
//import android.support.v4.app.NotificationCompat
//import android.support.v4.app.NotificationManagerCompat
//import android.util.Log
//import android.view.*
//import android.webkit.*
//import android.widget.FrameLayout
//import d.team.ecc.nicotube.NicoDmcAsync
//import d.team.ecc.nicotube.NicoDmcHeartbeatAsync
//import d.team.ecc.nicotube.R
//import d.team.ecc.nicotube.activities.MainActivity
//import d.team.ecc.nicotube.controller.PlayQueueData
//import d.team.ecc.nicotube.controller.VideoController
//import d.team.ecc.nicotube.database.DatabaseFront
//import d.team.ecc.nicotube.database.VideoInfo
//import kotlinx.android.synthetic.main.floating_item_filter.view.*
//import java.util.*
//
//
//
//
//class FloatingAppService : Service(){
//
//    companion object {
//
//        //ログ出力用タグ
//        private val TAG = "FloatingService"
//
//        //フローティング画面の操作でActivity開いたり、Serviceに処理実行させたりするのに必要な奴
//        @SuppressLint("StaticFieldLeak")
//        var context: Context? = null
//
//        //サービスに対してのアクションコード
//        const val ACTION_START = "start"
//        const val ACTION_STOP = "stop"
////        val ACTION_FLOATING = "floating"
//
//        //動画サイトID
//        const val SITE_YOUTUBE = 1
//        const val SITE_NICO = 2
//
//        //フローティング画面を作るのに必要な変数
//        @SuppressLint("StaticFieldLeak")
//        private var item: FloatingItem? = null
//        @SuppressLint("StaticFieldLeak")
//        private var view: View? = null
//        @SuppressLint("StaticFieldLeak")
//        var youTubePlayerView: View? = null
//        private var windowManager: WindowManager? = null
//
//        //YouTubeコントローラ用変数
//        var nowPlaying:Boolean = false
//        var playerStatus:Boolean = false
//        var youtubeDuration:Int = 0
//
//        //動画再生画質設定変数
//        var videoQuality:String = "default"
//
//
//        /**
//         * viewがオーバーレイ表示状態かどうかチェックする。
//         * not null -> true オーバーレイ表示
//         * null -> false オーバーレイ非表示
//         */
//        fun isOverlay(): Boolean{
//            item?.run {
//                Log.d(TAG, "isOverlay: true (item is living)")
//                return true
//            }
//            Log.d(TAG, "isOverlay: false (item is dead)")
//            return false
//        }
//
//        fun allowedFloating(){
//            item?.run{
//                floatingPermission = true
//            }
//        }
//
//        fun notAllowedFloating(){
//            item?.run{
//                floatingPermission = false
//            }
//        }
//
//        fun getWebView(): WebView?{
//            view?.run {
//                return floatingWebView
//            }
//            return null
//        }
//
//        /**
//         * 再生キューのテスト実装
//         * とりあえず選択した動画のデータベースに使用している情報の格納までは出来てる。
//         * サービスが持っている変数なので、画面が死んでも問題なし。
//         */
//        private var playQueueData: PlayQueueData = PlayQueueData()
//        private var RELATED_PLAYLIST: String = "RelatedPlayList"
//
//        /**
//         * 複数の動画をまとめたキューを作成する
//         * 再生リストから再生した時とか
//         */
//        fun setQueue(name: String,listItems: MutableList<VideoInfo>, position: Int){
//            clearQueue()
//
//            playQueueData.playListName = name
//            playQueueData.items = listItems
//            playQueueData.playingPosition = position
//
//            Log.d(TAG,"\nplayListName: ${playQueueData.playListName}\nplayListSize: ${playQueueData.items.size}")
//
//            //DBに履歴を登録
//            DatabaseFront.addHistory(listItems[position])
//        }
//
//        /**
//         * 検索結果から動画を選択し再生したときに関連動画再生キューを作成する
//         */
//        fun setQueue(videoInfo: VideoInfo){
//            clearQueue()
//
//            playQueueData.playListName = RELATED_PLAYLIST
//            playQueueData.items.add(videoInfo)
//            playQueueData.playingPosition = 0
//
//            Log.d(TAG,"\nplayListName: ${playQueueData.playListName}\n playListSize: ${playQueueData.items.size} \nvideoID: ${playQueueData.items.last().id}")
//
//            //DBに履歴を登録
//            DatabaseFront.addHistory(videoInfo)
//        }
//
//        /**
//         * 関連動画の再生キューに対して動画を追加する
//         * これで関連動画の先頭を連続再生させる
//         */
//        fun addQueue(videoInfo: VideoInfo){
//            Log.d("RelatedFragment","RelatedList: ${videoInfo.title}")
//            if(playQueueData.playListName == RELATED_PLAYLIST && playQueueData.items.last().id != videoInfo.id) {
//                playQueueData.items.add(videoInfo)
//            }
//            Log.d("RelatedFragment","add RelatedList: ${videoInfo.title}")
//        }
//
//        /**
//         * 再生キューの再生位置を指定の場所に変更する
//         */
//        fun changeQueuePosition(position: Int){
//            if(position >= 0 && position < playQueueData.items.size){
//                playQueueData.playingPosition = position
//                DatabaseFront.addHistory(playQueueData.items[position])
//            }
//        }
//
//
//        /**
//         * 再生キューのリスト情報を返す
//         */
//        fun getQueue(): MutableList<VideoInfo> {
//            return playQueueData.items
//        }
//
//
//        /**
//         * 現在再生中の動画情報を返す
//         */
//        fun getNowQueue(): VideoInfo?{
//
//            playQueueData.run{
//                return if(items.isNotEmpty() && playingPosition >= 0) items[playingPosition] else null
//            }
//        }
//
//
//        /**
//         * 再生キューの次の動画情報を取得する
//         */
//        fun getNextQueue(): VideoInfo?{
//
//            playQueueData.run {
//                if(playingPosition + 1 < items.size){
//                    val videoInfo = items[++playingPosition]
//                    DatabaseFront.addHistory(videoInfo)
//                    return videoInfo
//                }
//            }
//
//            return null
//        }
//
//        /**
//         * 再生キューの前の動画情報を取得する
//         */
//        fun getPrevQueue(): VideoInfo?{
//
//            playQueueData.run {
//                if(playingPosition - 1 >= 0){
//                    val videoInfo = items[--playingPosition]
//                    DatabaseFront.addHistory(videoInfo)
//                    return videoInfo
//                }
//            }
//
//            return null
//        }
//
//        /**
//         * 再生キューをリセットする
//         */
//        fun clearQueue(){
//
//            playQueueData.let {
//                it.items.clear()
//                it.playingPosition = -1
//                it.playListName = ""
//            }
//        }
//
//        /**
//         * 再生キューのテスト実装ここまで
//         */
//        val videoController = VideoController.getInstance()
//
//    }
//
//    private val notificationId = Random().nextInt()
//    private val channelId = "floatingservice_notification_channel"
//    lateinit var manager:NotificationManager
//    var videoCustomView: View? = null
//
//    /**
//     * ActivityのonCreate()と似たようなもの
//     */
//    @SuppressLint("SetJavaScriptEnabled", "InflateParams")
//    override fun onCreate(){
//        super.onCreate()
//
//        Log.d(TAG,"Call onCreate()")
//        //通知処理作成
//        val name = "NicoTube"
//        val importance = NotificationManager.IMPORTANCE_DEFAULT
//
//        lateinit var channel:NotificationChannel
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            channel = NotificationChannel(channelId,name,importance)
//            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(channel)
//        }
//
//        startNotification()
//
//        context = applicationContext
//        /**
//         * フローティング化(オーバーレイ表示)するための準備
//         * 一度表示したら変更することはない変数なので、ここでセットしておきます
//         */
//
//        val layoutInflater = LayoutInflater.from(this)
////        youTubePlayerView = layoutInflater.inflate(R.layout.floating_item_youtube_fragment,null)
////        youTubePlayerFragment = youTubePlayerView as YouTubePlayerFragment
//        view = layoutInflater.inflate(R.layout.floating_item_filter,null)
//        view?.run{
//
//            floatingWebView.settings.javaScriptEnabled = true
//            floatingWebView.settings.setAppCacheEnabled(true)
//            floatingWebView.webChromeClient = CustomWebChromeClient()
//
//            Log.d(TAG,"Height: ${floatingWebView.layoutParams.height} Width: ${floatingWebView.layoutParams.width}")
//            floatingWebView.webViewClient = object : WebViewClient() {
//                //プレイヤー内リンク外部アプリ遷移
//                @TargetApi(Build.VERSION_CODES.N)
//                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
//                    return true
//                }
//
//                @SuppressWarnings("deprecation")
//                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                    return true
//                }
//            }
//        }
//        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
//
//    }
//
//    /**
//     * 通知を表示しバックグラウンドからフォアグラウンド処理に移行する
//     * これにより、システム側からキルされにくく（キルされなく？）なります
//     */
//    private fun startNotification() {
//        val activityIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0)
//        val servicePlayIntent = Intent(this,FloatingAppService::class.java)
//        servicePlayIntent.action = "floating_service_play"
//        val pendingPlayIntent = PendingIntent.getService(this,1,servicePlayIntent,0)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val builder = NotificationCompat.Builder(this, channelId)
//                    .setContentIntent(pendingIntent)
//                    .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
//                            .setShowActionsInCompactView())
//                    .setSmallIcon(R.drawable.notification_icon_background)
//                    .setContentTitle(FloatingAppService::class.simpleName)
//                    .setContentText("Service is running.")
//                    .addAction(android.R.drawable.ic_media_play,"Play",pendingPlayIntent)
//            startForeground(notificationId, builder.build())
//        }else{
//            val notification = Notification.Builder(this)
//                    .setContentIntent(pendingIntent)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(FloatingAppService::class.simpleName)
//                    .setContentText("Service is running.")
//                    .build()
//            startForeground(notificationId, notification)
//        }
//    }
//
//    /**
//     * バインドされた時に呼び出され(てたような気がす)る
//     */
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    /**
//     * startServiceされた時に呼び出される。
//     * ここからオーバーレイ表示を開始したり中止したりする。
//     */
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if(intent == null || intent.action == ACTION_START){
//            Log.d(TAG,"SERVICE_START")
//            intent?.run {
//                videoQuality = intent.getStringExtra("Quality")
//            }
//                startOverlay()
//        }else{
//            Log.d(TAG,"SERVICE_STOP")
//            stopSelf()
//        }
//        return Service.START_STICKY
//    }
//
//    /**
//     * Serviceが停止したときに呼ばれる
//     */
//    override fun onDestroy() {
//        stopOverlay()
//        super.onDestroy()
//    }
//
//    /**
//     * オーバーレイ表示を開始・更新する。
//     * 受け取ったIntentからsiteIDでそれぞれの動画に合わせた処理に分岐、
//     * videoID,thumbnailURLを元に動画URLの取得・再生のための画面の描画までを行う。
//     */
//    private fun startOverlay() {
//
//        //ハートビート処理がされていたら新たに通信する前にハートビートを停止する
//        if (NicoDmcHeartbeatAsync.isHeartBeating()) {
//            Log.d(TAG,"Heart Beat Stop ...")
//            NicoDmcHeartbeatAsync.stopHeartBeatAsync()
//        }
//
//        getNowQueue()?.run {
//
//            when(siteId){
//                SITE_YOUTUBE -> {
//                    //<Iframe>タグを使用したパターン
//                    setYouTubeIframe(id,thumbnailURL)
//
//                    //<video>タグを使用したパターン
////                    setYouTubeHTML(id, thumbnailURL)
//
//                    setFloatingItemAction()
//                }
//
//                SITE_NICO -> {
//                    //<Iframe>タグを使用したパターン
////                    setNicoIframe(id,thumbnailURL)
//
//
//                    //<video>タグを使用したパターン
//                    //ニコニコ動画のDMCサーバーと非同期通信処理を行い、再生する動画URLの取得を行う
//                    val url = NicoDmcAsync.NICO_URL_ENDPOINT + id
//                    Log.d("${TAG}_NicoDmc",url)
//
//
//                    object : NicoDmcAsync() {
//                        /**
//                         * DMCサーバーとの通信処理が終了したら、フローティング画面の更新とハートビート送信を行う
//                         * doInBackgroundの返り値が引数
//                         * @param resultItems MaxLength = 3
//                         * resultItems[0] = 動画URL
//                         * resultItems[1] = 投稿されたコメント(JSONデータ)
//                         * resultItems[2] = ハートビート送信の使用するデータ
//                         */
//                        override fun onPostExecute(resultItems: List<String>) {
//                            super.onPostExecute(resultItems)
//                            Log.d("${TAG}_NicoDmc", "Success")
//                            Log.d("${TAG}_NicoDmc", "VideoURL : ${resultItems[0]}")
//
//                            //フローティング画面に表示する内容をセットする
//                            setNicoHTML(resultItems[0], thumbnailURL,url)
//
//                            //サービスに対して要求されたアクションコードに基づき、フローティング画面の作成・更新を行う
//                            setFloatingItemAction()
//
//                            //ハートビート送信をスタートする
//                            if(!resultItems[2].equals("empty")){
//                                NicoDmcHeartbeatAsync.startHeartBeatAsync(resultItems[2])
//                            }
//                        }
//                    }.execute(url)
//                }
//
//                else -> {
//                    Log.d(TAG,"SITE ID is WRONG")
//                }
//            }
//        }
//    }
//
//
//    /**
//     * ニコニコ動画のIframeでの再生の準備を行う
//     *
//     * @param videoID 動画ID
//     * @param thumbnailURL サムネイルURL
//     */
//    private fun setNicoIframe(videoID: String, thumbnailURL: String){
//        view?.run{
//            var data: String = "<style>* {margin:0; padding:0;}</style><iframe id=\"nicoPlayer\" src=\"http://embed.nicovideo.jp/watch/$videoID?jsapi=1&playerId=1\" width=\"100%\" height=\"100%\" frameborder=\"0\" allowfullscreen></iframe>"
//
//            floatingWebView.loadData(data,"text/html","UTF-8")
//        }
//    }
//
//    /**
//     * YouTubeのIframeでの再生の準備を行う
//     *
//     * @param videoID 動画ID
//     * @param thumbnailURL サムネイルURL
//     */
//    private fun setYouTubeIframe(videoID: String, thumbnailURL: String){
//        view?.run{
////            val data: String = "" +
////                    "<iframe id=\"ytplayer\" type=\"text/html\" src=\"https://www.youtube.com/embed/$videoID?version=3&enablejsapi=1&rel=0&controls=0&showinfo=0&autoplay=1&autohide=1\"" +
////                    " width=\"100%\" height=\"100%\" frameborder=\"0\" allowfullscreen ></iframe>"
//            val data:String="<!DOCTYPE html>\n" +
//                    "<html>\n" +
//                    "  <head>\n" +
//                    "    <style>\n" +
//                    "      div.player{font-size:0;}\n" +
//                    "    </style>\n" +
//                    "  </head>\n" +
//                    "  <body>\n" +
//                    "    <!-- 1. The <iframe> (and video player) will replace this <div> tag. -->\n" +
//                    "    <style>* {margin:0; padding:0; height:100%; width:100%;}</style>\n" +
//                    "    <div class=\"player\">\n" +
//                    "    <div id=\"player\"></div>\n" +
//                    "    </div>\n" +
//                    "    <script src=\"file:///android_asset/youtube-player.js\">\n" +
//                    "    \n" +
//                    " \n" +
//                    "    </script>\n" +
//                    "    <script>\n" +
//                    "    var vId = \""+videoID+"\";\n" +
//                    "    window.onload = function(){\n" +
//                    "\tloadVideoData();\n" +
//                    "      \n" +
//                    "    }\n" +
//                    "      //画面ロード時に動画IDの読み込み処理を行う\n" +
//                    "    function loadVideoData() {\n" +
//                    "\t\tloadPlayer(vId);\n" +
//                    "    }\n" +
//                    "    </script>\n" +
//                    "  </body>\n" +
//                    "</html>"
//            floatingWebView.loadDataWithBaseURL("https://youtube.com", data
//                    , "text/html", "UTF-8",null)
//
//            floatingWebView.webChromeClient = object : WebChromeClient() {
//                override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
//                    when(message){
//                        "youtubeReady" ->{
//                            playerStatus = true
//                            floatingWebView.loadUrl("javascript:player.playVideo();")
//                            videoController.changeButton(false)
//                        }
//                        "youtubeStart" ->{
//                            floatingWebView.loadUrl("javascript:getPlayQuality();")
//                            floatingWebView.loadUrl("javascript:getPlayQualityAvailable();")
//                            nowPlaying = true
//                            videoController.changeButton(true)
//                        }
//                        "youtubeStop" ->{
//                            nowPlaying = false
//                            videoController.changeButton(false)
//                        }"Ended"-> {
//                            if(getNextQueue() != null) startOverlay()
//                            videoController.endNotification()
//                        }else->{
//                            if(message.contains("Duration")){
//                                videoController.duration(message)
//                            }else if(message.contains("Current")){
//                                videoController.current(message)
//                            }
//                        }
//                    }
//                    Log.d("youtubedebugmessage",message)
//                    result.cancel()  /* ←これが必要 */
//                    return true      /* ダイアログ表示しない */
//                }
//            }
//        }
//    }
//
//
//    /**
//     * フローティング画面のWebViewに表示する内容をセットする
//     * @param videoURL DMCサーバーから取得した動画URL
//     * @param thumbnailURL 動画のサムネイルURL
//     */
//    private fun setNicoHTML(videoURL: String, thumbnailURL: String, url: String) {
////        val html_s = "<html lang=\"ja\">\n"
////        val head = "<head>\n" +
////                "  <link href=\"http://vjs.zencdn.net/5.10.8/video-js.css\" rel=\"stylesheet\">\n" +
////                "\n" +
////                "  <!-- If you'd like to support IE8 -->\n" +
////                "<script>document.cookie=</script>" +
////                "<script src=\"http://vjs.zencdn.net/ie8/1.1.2/videojs-ie8.min.js\"></script>\n" +
////                "</head>"
////        val body = "<body>\n" +
////                "<video id=\"my-video\" class=\"video-js\" controls preload=\"auto\" poster=\"$thumbnailURL\" style=\"position: absolute; width: 100%; height: 100%; top: 0px; left: 0px; bottom: 0px; right: 0px; display: block;\" data-setup=\"{}\">\n" +
////                "  <source src=\"$videoURL\" type='video/mp4'>\n" +
////                "  <p class=\"vjs-no-js\">\n" +
////                "    To view this video please enable JavaScript, and consider upgrading to a web browser that\n" +
////                "    <a href=\"http://videojs.com/html5-video-support/\" target=\"_blank\">supports HTML5 video</a>\n" +
////                "  </p>\n" +
////                "</video>" +
////                "  <script src=\"http://vjs.zencdn.net/5.10.8/video.js\"></script>\n" +
////                "</body>"
////        val html_e = "</html>"
////        //
////        val html = html_s + head + body + html_e
//        val html="""
//            <html lang="ja">
//                <head>
//                    <link href="http://vjs.zencdn.net/5.10.8/video-js.css" rel="stylesheet">
//                    <link href="file:///android_asset/nicoNormal.css" rel="stylesheet">
//                    <!-- If you'd like to support IE8 -->
//                    <script src="http://vjs.zencdn.net/ie8/1.1.2/videojs-ie8.min.js"></script>
//                </head>
//                <body>
//                    <video id="my-video" class="video-js" controls preload="auto" poster="$thumbnailURL" style="position: absolute; width: 100%; height: 100%; top: 0px; left: 0px; bottom: 0px; right: 0px; display: block;" data-setup="{}">
//                        <source src="$videoURL" type='video/mp4'>
//                        <p class="vjs-no-js">
//                            <a href="https://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a>
//                        </p>
//                    </video>
//                    <script src="http://vjs.zencdn.net/5.10.8/video.js"></script>
//                    <script>
//                        var myPlayer = videojs('my-video');
//                        var setTime;
//                        function nicoStatusCheck(){
//                        clearInterval(setTime);
//                        if(myPlayer.paused()){
//                            alert('nicoStop');
//
//                        }
//                        else{
//                            alert('nicoStart');
//                            setTime = setInterval(function() {
//                            var currentTime = Math.floor(myPlayer.currentTime());
//                             alert('Current'+currentTime);
//                             alert('Duration'+Math.floor(myPlayer.duration()));}, 1000);
//                        }
//                        }
//
//                        videojs("my-video").ready(function(){
//                        var myPlayer = this;
//                        alert('nicoReady');
//                        });
//                        var video = document.querySelector('video');
//                        video.onended = function(){
//                        alert('Ended');
//                        }
//                    </script>
//                </body>
//            </html>
//            """
////        val html = "\n" +
////                "<!DOCTYPE html>\n" +
////                "<meta charset=\"utf-8\">\n" +
////                "<title>jsplayer</title>\n" +
////                "<!--  ■jsplayer http://wp.me/ppmtz-1Cu  -->\n" +
////                "\n" +
////                "<link rel=\"stylesheet\" href=\"file:///android_asset/nicoStyle.css\">\n" +
////                "\n" +
////                "<div class=\"jsplayer\">\n" +
////                "  <div class=\"jsplayer-screen\" tabindex=\"1\">\n" +
////                "<style>* {margin:0; padding:0; height:100%; width:100%;}</style>\n" +
////                "    <video class=\"jsplayer-video\" poster=\"$thumbnailURL\" autoplay></video>\n" +
////                "  </div>\n" +
////                "</div>" +
////                "<script src=\"file:///android_asset/script1.js\"></script>\n" +
////                "<script>\n" +
////                "\$v.query = \$v.fn.deparam(location.search);" +
////                "\$v.comment.list = [[['うん',0.5]]];\n" +
////                "\$v.comment.get();" +
////                "\$v.entrypoint('http://pa904d5a10.dmc.nico/hlsvod/ht2_nicovideo/nicovideo-so33354846_ff1600262f27cccf34acf05fffecc5cbd93bb09a4b61444158967b55cb500984/master.m3u8?ht2_nicovideo=14495028.wsznj6_pagcu0_19tn9ltnr5azc');\n" +
////                "\$v.comment.get();\n" +
////                "</script>\n"+
////                "\n"
//
//        view?.run {
//            floatingWebView.stopLoading()
//            floatingWebView.loadDataWithBaseURL(url,html, "text/html", "utf-8",null)
//            floatingWebView.webChromeClient = object : WebChromeClient() {
//                override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
//                    when(message){
//                        "nicoReady" ->{
//                            floatingWebView.loadUrl("javascript:myPlayer.play();")
//                            playerStatus = true
//                            floatingWebView.loadUrl("javascript:nicoStatusCheck();")
//                        }
//                        "nicoStart" ->{
//                            nowPlaying = true
//                            videoController.changeButton(true)
//                        }
//                        "nicoStop" -> {
//                            nowPlaying = false
//                            videoController.changeButton(false)
//                            Log.d("nicostop","nicostop")
//                        }"Ended"-> {
//                            if(getNextQueue() != null) startOverlay()
//                            videoController.endNotification()
//                        }else->{
//                            if(message.contains("Duration")){
//                                videoController.duration(message)
//                            }else if(message.contains("Current")){
//                                videoController.current(message)
//                            }
//                        }
//                    }
//                    Log.d("youtubedebugmessage",message)
//                    result.cancel()  /* ←これが必要 */
//                    return true      /* ダイアログ表示しない */
//                }
//            }
//        }
//    }
//
//    private fun setYouTubeHTML(videoID: String?, thumbnailURL: String?) {
//        Log.d("${TAG}_YouTube","videoID = $videoID")
//        Log.d("${TAG}_YouTube","thumbnailURL = $thumbnailURL")
//
//        val html = "<html>\n" +
//                "  <head>\n" +
//                "    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n" +
//                "    <meta name=\"robots\" content=\"noindex, nofollow\">\n" +
//                "    <meta name=\"googlebot\" content=\"noindex, nofollow\">\n" +
//                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
//                "    <script type=\"text/javascript\" src=\"https://code.jquery.com/jquery-git.js\"></script>\n" +
//                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://jsfiddle.net/css/normalize.css\">\n" +
//                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://jsfiddle.net/css/result-light.css\">\n" +
////                ここから youtube風のプレイヤーにするための部分
//                "  <link href=\"http://vjs.zencdn.net/5.10.8/video-js.css\" rel=\"stylesheet\">\n" +
//                "\n" +
//                "  <!-- If you'd like to support IE8 -->\n" +
//                "  <script src=\"http://vjs.zencdn.net/ie8/1.1.2/videojs-ie8.min.js\"></script>\n" +
////                ここまで youtube風のプレイヤーにするための部分
//                "    <title>Youtube Video Background by 350D</title>\n" +
//                "    <script type=\"text/javascript\">\n" +
//                "\$(window).on('load', function() {\n" +
//                "\n" +
//                "var vid = \"$videoID\",\n" +
//                "    streams,\n" +
//                "    video_focused = true,\n" +
//                "    video_tag = \$(\"#video\"),\n" +
//                "    video_obj = video_tag.get(0);\n" +
//                "\$.getJSON(\"https://query.yahooapis.com/v1/public/yql\", {\n" +
//                "    q: \"select * from csv where url='https://www.youtube.com/get_video_info?video_id=\" + vid + \"'\",\n" +
//                "    format: \"json\"\n" +
//                "}, function(data) {\n" +
//                "    if (data.query.results && !data.query.results.row.length) {\n" +
//                "        streams = parse_youtube_meta(data.query.results.row.col0);\n" +
//                "        video_tag.attr({\n" +
//                "            src: streams['1080p'] || streams['720p'] || streams['360p']\n" +
//                "        });\n" +
//                "    } else {\n" +
//                "    \t\t\$('pre').text('YQL request error...');\n" +
//                "    }\n" +
//                "});\n" +
//                "\n" +
//                "function parse_youtube_meta(rawdata) {\n" +
//                "    var data = parse_str(rawdata),\n" +
//                "        streams = (data.url_encoded_fmt_stream_map + ',' + data.adaptive_fmts).split(','),\n" +
//                "        result = {};\n" +
//                "    \$.each(streams, function(n, s) {\n" +
//                "        var stream = parse_str(s),\n" +
//                "            itag = stream.itag * 1,\n" +
//                "            quality = false,\n" +
//                "            itag_map = {\n" +
//                "                18: '360p',\n" +
//                "                22: '720p',\n" +
//                "                37: '1080p',\n" +
//                "                38: '3072p',\n" +
//                "                82: '360p3d',\n" +
//                "                83: '480p3d',\n" +
//                "                84: '720p3d',\n" +
//                "                85: '1080p3d',\n" +
//                "                133: '240pna',\n" +
//                "                134: '360pna',\n" +
//                "                135: '480pna',\n" +
//                "                136: '720pna',\n" +
//                "                137: '1080pna',\n" +
//                "                264: '1440pna',\n" +
//                "                298: '720p60',\n" +
//                "                299: '1080p60na',\n" +
//                "                160: '144pna',\n" +
//                "                139: \"48kbps\",\n" +
//                "                140: \"128kbps\",\n" +
//                "                141: \"256kbps\"\n" +
//                "            };\n" +
//                "        //if (stream.type.indexOf('o/mp4') > 0) console.log(stream);\n" +
//                "        if (itag_map[itag]) result[itag_map[itag]] = stream.url;\n" +
//                "    });\n" +
//                "    return result;\n" +
//                "};\n" +
//                "\n" +
//                "function parse_str(str) {\n" +
//                "    return str.split('&').reduce(function(params, param) {\n" +
//                "        var paramSplit = param.split('=').map(function(value) {\n" +
//                "            return decodeURIComponent(value.replace('+', ' '));\n" +
//                "        });\n" +
//                "        params[paramSplit[0]] = paramSplit[1];\n" +
//                "        return params;\n" +
//                "    }, {});\n" +
//                "}\n" +
//                "\n" +
//                "\n" +
//                "});\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "</script>\n" +
//                "  </head>\n" +
//                "  <body>\n" +
//                "    <video id=video class=video-js controls preload=\"auto\" " +
//                "           style=\"position: absolute; width: 100%; height: 100%; top: 0px; left: 0px; bottom: 0px; right: 0px; display: block;\""+
//                "           poster=\"$thumbnailURL\"" +
//                "           data-setup={}>\n" +
//                "                <p class=vjs-no-js> \n" +
//                "                    To view this video please enable JavaScript, and consider upgrading to a web browser that\n" +
//                "                    <a href=\"http://videojs.com/html5-video-support/\" target=_blank>supports HTML5 video</a>\n" +
//                "                  </p> \n" +
//                "                </video>\n" +
//                "  <script src=\"http://vjs.zencdn.net/5.10.8/video.js\"></script>\n" +
//                "  </body>\n" +
//                "</html>"
//
//        view?.run {
//            Log.d("videoID",videoID)
//            floatingWebView.settings.javaScriptEnabled = true
//            floatingWebView.settings.setAppCacheEnabled(true)
//            floatingWebView.loadDataWithBaseURL("http://www.youtube.com/",html,"text/html","utf-8",null)
//
//        }
//    }
//
//    /**
//     * FloatingItemへ画面作成・更新処理を投げる
//     */
//    private fun setFloatingItemAction(){
//        view?.run {
//            item?.run{
//                Log.d(TAG," FloatingItem ActionCode: UPDATE")
//                actionCode = FloatingItem.ACTION_UPDATE
//                return
//            }
//
//            Log.d(TAG," FloatingItem ActionCode: START")
//            item = FloatingItem(windowManager!!, this).apply {
//                actionCode = FloatingItem.ACTION_VISIBLE
//            }
//        }
//    }
//
//    /**
//     * オーバーレイ表示を中止する
//     */
//    private fun stopOverlay() {
//        //フローティング画面を非表示にする
//        item?.run {
//            actionCode = FloatingItem.ACTION_UNVISIBLE
//        }
//
//        //ニコニコ動画のハートビート送信を停止する
//        NicoDmcHeartbeatAsync.stopHeartBeatAsync()
//
//        //それぞれの変数をnullにする
//        item = null
//        view = null
//        windowManager = null
//        youTubePlayerView = null
//    }
//
//
//
//
//    /**
//     * 参考にするサイトで必要そうだったやつ
//     * 記述途中ですが後々必要になるかもしれないので一応残しておきます。
//     *
//     * …どこのサイトやろ？一応残しておきますが多分使わないです。
//     */
//    class CustomWebChromeClient : WebChromeClient(){
//        private var videoCustomView: View? = null
//        private var customViewContainer: FrameLayout? = null
//
//        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
//            if(videoCustomView != null){
//                callback?.onCustomViewHidden()
//                return
//            }
//
//            val frame: FrameLayout = view as FrameLayout
//            val view1: View = frame.getChildAt(0)
//            view.layoutParams = FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    Gravity.CENTER
//            )
//            view1.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
//                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
//                    onHideCustomView()
//                    return@OnKeyListener true
//                }
//                false
//            })
//
//        }
//    }
//
//}
//
