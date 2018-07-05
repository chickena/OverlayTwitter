//package d.team.ecc.nicotube.floating
//
//import android.content.Intent
//import android.graphics.PixelFormat
//import android.os.Build
//import android.util.Log
//import android.view.*
//import d.team.ecc.nicotube.App
//import d.team.ecc.nicotube.OriginalGestureDetector
//import d.team.ecc.nicotube.activities.FloatingPlayerActivity
//import d.team.ecc.nicotube.askDistance
//import kotlinx.android.synthetic.main.floating_item_filter.view.*
//
///**
// * 実際にオーバーレイ表示をしたり、
// * フローティングのアイテムにリスナーを付けて動かせるようにしているところ
// */
//class FloatingItem(val windowManager: WindowManager, val view: View): OriginalGestureDetector() {
//
//    companion object {
//        private val TAG = FloatingItem::class.qualifiedName
//
//        //フローティング画面の処理コード
//        const val ACTION_VISIBLE = 1
//        const val ACTION_UPDATE = 2
//        const val ACTION_UNVISIBLE = 0
//
//        //フローティング画面のonFling処理のフリック判定基準
//        private const val SWIPE_MIN_DISTANCE = 150
//        private const val SWIPE_MAX_OFF_PATH = 100
//        private const val SWIPE_THRESHOLD_VELOCITY = 4000
//    }
//
//
//    private val params =
//            WindowManager.LayoutParams(
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            if (Build.VERSION.SDK_INT >= 26){
//                //Android8.0以上の端末の時の処理
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            }else{
//                //Android8.0未満の端末の時の処理
//                WindowManager.LayoutParams.TYPE_PHONE
//            },
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            PixelFormat.TRANSLUCENT)
//            .apply {
//                gravity = Gravity.TOP or Gravity.START
//                x = 0
//                y = 0
//            }
//
//    /**
//     * Activity側からオーバーレイ表示・非表示・画面の更新を行う
//     *
//     */
//    var actionCode: Int = ACTION_UNVISIBLE
//        set(value) {
//            if (field != value){
//
//                when(value){
//                    ACTION_UNVISIBLE -> {
//                        //現在のアクションコードを保持し、画面を非表示にする。
//                        field = value
//                        windowManager.removeView(view)
//                    }
//
//                    ACTION_VISIBLE -> {
//                        //現在のアクションコードを保持し、画面を表示にする。フローティングを非許可にする。
//                        field = value
//                        floatingPermission = false
//                        //Activity、動画を選択したってことで、動画画面起動。-> 画面上部に表示固定
//                        view.floatingWebViewFilter.layoutParams.height = App.maxPlayerSize.height
//                        view.floatingWebViewFilter.layoutParams.width = App.maxPlayerSize.width
//                        view.floatingWebView.layoutParams.height = App.maxPlayerSize.height
//                        view.floatingWebView.layoutParams.width = App.maxPlayerSize.width
//                        view.floatingWebViewFilter.visibility = View.INVISIBLE
//                        params.position = App.defaultPositionForMaxPlayer
//
//                        windowManager.addView(view, params)
//                    }
//
//                    ACTION_UPDATE -> {
//                        //直前の画面モードのままで画面の表示を更新するだけの処理に変更
//                        windowManager.updateViewLayout(view,params)
//                    }
//                }
//            }
//        }
//
//    /**
//     * フローティング画面の操作を許可するかどうかを決定する変数
//     */
//    var floatingPermission: Boolean = false
//        set(value) {
//            if(field != value){
//                field = value
//                when(field){
//                //許可されるのは動画再生画面が破棄された時
//                    true -> {
//                        view.floatingWebViewFilter.visibility = View.VISIBLE
//                        Log.d(TAG,"Floating Allowed")
//
//                        //動画再生画面から外れたってことで、floatingを許可。デフォルト設定のフローティング画面位置は画面右下。
//                        view.floatingWebViewFilter.layoutParams.height = App.minPlayerSize.height
//                        view.floatingWebViewFilter.layoutParams.width = App.minPlayerSize.width
//                        view.floatingWebView.layoutParams.height = App.minPlayerSize.height
//                        view.floatingWebView.layoutParams.width = App.minPlayerSize.width
//                        params.position = App.defaultPositionForMinPlayer
//
////                        setListeners()
//                        setOnGestureListener()
//
//                        windowManager.updateViewLayout(view,params)
//                    }
//
//                //非許可にされるのはMainActivityで動画を選択したとき
//                    false -> {
//                        view.floatingWebViewFilter.visibility = View.INVISIBLE
//                        Log.d(TAG,"Floating Not Allowed")
//                        view.floatingWebViewFilter.layoutParams.height = App.maxPlayerSize.height
//                        view.floatingWebViewFilter.layoutParams.width = App.maxPlayerSize.width
//                        view.floatingWebView.layoutParams.height = App.maxPlayerSize.height
//                        view.floatingWebView.layoutParams.width = App.maxPlayerSize.width
//                        params.position = App.defaultPositionForMaxPlayer
//
////                        resetListeners()
//                        resetOnGestureListener()
//
//                        windowManager.updateViewLayout(view,params)
//
//                    }
//                }
//            }
//        }
//
//
//
//    /**
//     * フローティング画面のポジションの移動など。
//     * 画面の大きさや場所の移動などあればここで画面の更新がされます。
//     */
//    private var initial: Position = Position(0f, 0f)
//
//    //onTouchで処理するバージョンで使用。GestureListenerでは要らない
//    private var startDistance = 0
//    private var changedDistance = 0
//    private var testPosition1: MutableList<Position> = mutableListOf(Position(0f, 0f), Position(0f, 0f))
//    private var testPosition2: MutableList<Position> = mutableListOf(Position(0f, 0f), Position(0f, 0f))
//
//
//    init {
//        resetOnGestureListener()
//    }
//
//    private val MotionEvent.position: Position
//        get() = Position(rawX, rawY)
//
//    private var WindowManager.LayoutParams.position: Position
//        get() = Position(x.toFloat(), y.toFloat())
//        set(value) {
//            x = value.x
//            y = value.y
//        }
//
//    /**
//     * フローティング画面の操作を有効にする。
//     * 動画再生画面非表示中
//     */
//    private fun setOnGestureListener(){
//        view.floatingWebViewFilter.setOnGestureListener(object : GestureDetector.SimpleOnGestureListener(){
//
//            /**
//             * フローティング画面を消す（サービスを停止させる）
//             */
//            override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
//                Log.d("${TAG}_Gesture","onFling()")
//                try {
//                    // 移動距離・スピードを出力
//                    val distance_y = Math.abs(event1.y - event2.y)
//                    val velocity_y = Math.abs(velocityY)
//                    Log.d("${TAG}_Gesture", "縦の移動距離:$distance_y 縦の移動スピード:$velocity_y" )
//
//                    // X軸の移動距離が大きすぎる場合
//                    if (Math.abs(event1.x - event2.x) > SWIPE_MAX_OFF_PATH) {
//                        Log.d("${TAG}_Gesture","横の移動距離が大きすぎます")
//
//                    // 開始位置から終了位置の移動距離が指定値より大きい
//                    // Y軸の移動速度が指定値より大きい
//                    } else if (event2.y - event1.y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                        Log.d("${TAG}_Gesture","上から下")
//
//                        FloatingAppService.context?.run {
//                            Log.d("${TAG}_Gesture","Create Intent: $this")
//                            val intent = Intent(this, FloatingAppService::class.java)
//                                    .setAction(FloatingAppService.ACTION_STOP)
//                            startService(intent)
//                        }
//
//                    // 終了位置から開始位置の移動距離が指定値より大きい
//                    // Y軸の移動速度が指定値より大きい
//                    } else if (event1.y - event2.y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                        Log.d("${TAG}_Gesture","下から上")
//
//                        FloatingAppService.context?.run {
//                            Log.d("${TAG}_Gesture","Create Intent: $this")
//                            val intent = Intent(this, FloatingAppService::class.java)
//                                    .setAction(FloatingAppService.ACTION_STOP)
//                            startService(intent)
//                        }
//                    }
//
//                } catch (e: Exception) {
//
//                }
//
//                return false
//            }
//
//
//            /**
//             * スクロールで画面を移動させる
//             * ＊この次にonFlingが呼ばれるから処理に注意＊
//             */
//            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
//                Log.d("${TAG}_Gesture","onScroll()")
//
//                initial.let{
//                    params.position = it + e2.position
//                    windowManager.updateViewLayout(view, params)
//                }
//
//                return false
//            }
//
//            /**
//             * ダブルタップで動画画面呼び出す
//             */
//            override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
//                Log.d("${TAG}_Gesture","onDoubleTapEvent()")
//
//                e?.run{
//                    when(action){
//                        MotionEvent.ACTION_DOWN -> {
//                            Log.d("${TAG}_Gesture","onDoubleTap_DOWN")
//
//                        }
//
//                        MotionEvent.ACTION_MOVE -> {
//                            Log.d("${TAG}_Gesture","onDoubleTap_MOVE")
//
//                        }
//
//                        MotionEvent.ACTION_UP -> {
//                            Log.d("${TAG}_Gesture","onDoubleTap_UP")
//                            FloatingAppService.context?.run {
//                                Log.d("${TAG}_Gesture","Create Intent: $this")
//
//                                val intent = Intent(this, FloatingPlayerActivity::class.java)
//                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                startActivity(intent)
//                            }
//                        }
//
//                        else -> {
//                            Log.d("${TAG}_Gesture","onDoubleTap_OTHER")
//
//                        }
//                    }
//                }
//
//                return false
//            }
//
//            /**
//             * 画面に触れたとき
//             */
//            override fun onDown(event: MotionEvent): Boolean {
//                Log.d("${TAG}_Gesture","onDown()")
//                initial = params.position - event.position
//                return true
//            }
//
//        })
//
//    }
//
//
//    /**
//     * onTouchで処理するバージョン
//     * 画面の拡大・移動が可能
//     */
//    private fun setListeners() {
//
//
//        //フローティング画面にリスナーをセットする
//        view.floatingWebViewFilter.setOnTouchListener {
//            view, event ->
//            Log.d(TAG,"PointerCount = "+event.pointerCount)
//            Log.d(TAG,"ActionCode = "+event.action.toString())
//
//
//            //アクション内容で処理分岐
//            when (event.action) {
//            //タップしたとき ActionCode = 0
//                MotionEvent.ACTION_DOWN -> {
//
//                    initial = params.position - event.position
//                    Log.d(TAG, "ACTION_DOWN")
//
//                    Log.d(TAG, "PointerID = 0 getX: ${event.getX(0)} getY: ${event.getY(0)}")
//
//                }
//
//            //タップしたまま動かした時(結構感度いいのか押し続けてるだけでも反応します) ActionCode = 2
//                MotionEvent.ACTION_MOVE -> {
//                    //動かした時のタップしている数によって処理分岐
//                    when (event.pointerCount) {
//                    //シングルタップ
//                        1 -> {
//                            //フローティング画面の表示位置を変更する
//                            initial?.let {
//                                params.position = it + event.position
//                                windowManager.updateViewLayout(this.view, params)
////                        Log.d(TAG,"ACTION_MOVE")
//                            }
//                        }
//                    //マルチタップ
//                        2 -> {
//                            Log.d(TAG, "ACTION_MULTI_MOVE")
//
//                            //画面の大きさを変更する処理のテスト(マルチタップするたびに大きくなっていく)
//                            initial?.let {
//                                var width = this.view.floatingWebViewFilter.width
//                                var height = this.view.floatingWebViewFilter.height
////                                Log.d(TAG,"width = " + width + "\nheight = " + height)
//
//                                for (i in 0..1) {
//                                    testPosition2[i] = (Position(event.getX(i), event.getY(i)))
//
//                                    //X:横 Y:縦
//                                    Log.d("${TAG}_MULTI", "ID = $i getX: ${event.getX(i)} getY: ${event.getY(i)}")
//                                    Log.d("${TAG}_MULTI", "ID = $i X: ${testPosition2[i].x} Y: ${testPosition2[i].y}")
//                                }
//
//                                changedDistance = askDistance(testPosition2)
//
//                                val scaling = changedDistance.toDouble() / startDistance.toDouble()
//
//                                Log.d("${TAG}_MULTI", "Distance: $startDistance -> $changedDistance  Scaling: $scaling")
//
//                                //WebViewの拡大処理
//                                var webViewParams = this.view.floatingWebViewFilter.layoutParams
//                                Log.d("${TAG}_MULTI", "Height: ${webViewParams.height} Width: ${webViewParams.width}")
//                                this.view.floatingWebViewFilter.layoutParams = App.reSizeFloatingWebView(webViewParams, scaling)
//                                this.view.floatingWebView.layoutParams = App.reSizeFloatingWebView(webViewParams, scaling)
//
//                                windowManager.updateViewLayout(this.view, params)
//                            }
//
//                        }
//                    //タップしている指を離したとき ActionCode = 1
//                        MotionEvent.ACTION_UP -> {
////                            initial = null
//                            Log.d(TAG, "ACTION_UP")
//                        }
//
//                    //2本目以降の指が置かれた時
//                        261 -> {
//                        }
//                    }
//
//                    Log.d("${TAG}_MULTI", "ACTION_POINTER_DOWN")
//
//                    if (event.pointerCount == 2) {
//
//                        for (i in 0..1) {
//                            testPosition1[i] = (Position(event.getX(i), event.getY(i)))
//
//                            //X:横 Y:縦
//                            Log.d("${TAG}_MULTI", "PointerID = $i getX: ${event.getX(i)} getY: ${event.getY(i)}")
//                            Log.d("${TAG}_MULTI", "PointerID = $i X: ${testPosition1[i].x} Y: ${testPosition1[i].y}")
//                        }
//
//                        startDistance = askDistance(testPosition1)
//
////                        webViewParams = this.view.floatingWebView.layoutParams
//                    }
//                }
//
//            //2本目以降の指が離された時
//                262 -> {
//                    Log.d("${TAG}_MULTI", "ACTION_POINTER_UP")
//
//                    //reset
////                    testPosition1.clear()
////                    testPosition2.clear()
//
//                }
//            }
//
//            false
//        }
//
//
//        view.floatingWebViewFilter.setOnClickListener {
//            Log.d(TAG, "onClick")
//        }
//    }
//
//    /**
//     * フローティング画面の操作を無効化する。
//     * 動画再生画面表示中
//     */
//    private fun resetOnGestureListener(){
//        view.floatingWebViewFilter.setOnGestureListener(null)
//    }
//
//    /**
//     * onTouchで処理するバージョンのリスナーリセット処理
//     */
//    private fun resetListeners() {
//        view.floatingWebViewFilter.setOnClickListener(null)
//        view.floatingWebViewFilter.setOnTouchListener(null)
////        view.floatingWebViewFilter.setOnGestureListener(null)
//    }
//
//
////    private data class Position(val fx: Float, val fy: Float){
////        val x: Int
////        get() = fx.toInt()
////
////        val y: Int
////        get() = fy.toInt()
////
////        operator fun plus(p: Position) = Position(fx + p.fx, fy + p.fy)
////        operator fun minus(p: Position) = Position(fx - p.fx, fy - p.fy)
////    }
//
//}