package com.otameshi.main.overlaytwitter

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.overlay.view.*

class FloatingItem(val windowManager: WindowManager, val view: View) {
    companion object {
        private val TAG = FloatingItem::class.qualifiedName
        //フローティング画面のonFling処理のフリック判定基準
        private const val SWIPE_MIN_DISTANCE = 150
        private const val SWIPE_MAX_OFF_PATH = 100
        private const val SWIPE_THRESHOLD_VELOCITY = 4000
    }

    private val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= 26) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,

            PixelFormat.TRANSLUCENT)
            .apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 100
            }

    var visible: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {

                    view.viewTest.layoutParams.height = 650
                    view.viewTest.layoutParams.width = 400
                    view.floatingWebView.layoutParams.height = 600
                    view.floatingWebView.layoutParams.width =  400
                    setOnGestureListener()
                    windowManager.addView(view, params)
                } else {
                    windowManager.removeView(view)
                }
            }
        }

    private var initial: Position? = null

    init {
        view.viewTest.setOnTouchListener { view, event ->
            Log.d(TAG, event.action.toString())
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initial = params.position - event.position
                    Log.d(TAG, "ACTION_DOWN")
                }
                MotionEvent.ACTION_MOVE -> {
                    initial?.let {
                        params.position = it + event.position
                        windowManager.updateViewLayout(this.view, params)
                        Log.d(TAG, "ACTION_MOVE")
                    }
                }
                MotionEvent.ACTION_UP -> {
                    initial = null
                    Log.d(TAG, "ACTION_UP")
                }
            }
            false
        }
        view.viewTest.setOnClickListener {
            Log.d(TAG, "onClick")
        }
    }

    private val MotionEvent.position: Position
        get() = Position(rawX, rawY)

    private var WindowManager.LayoutParams.position: Position
        get() = Position(x.toFloat(), y.toFloat())
        set(value) {
            x = value.x
            y = value.y
        }

    private data class Position(val fx: Float, val fy: Float) {
        val x: Int
            get() = fx.toInt()

        val y: Int
            get() = fy.toInt()

        operator fun plus(p: Position) = Position(fx + p.fx, fy + p.fy)
        operator fun minus(p: Position) = Position(fx - p.fx, fy - p.fy)
    }

    private fun setOnGestureListener(){
        view.viewTest.setOnGestureListener(object : GestureDetector.SimpleOnGestureListener(){
            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                Log.d(TAG,"onFling()")
                try{
                    // 移動距離・スピードを出力
                    val distance_y = Math.abs(e1.y - e2.y)
                    val velocity_y = Math.abs(velocityY)
                    Log.d("MyView_onFling", "縦の移動距離:$distance_y 縦の移動スピード:$velocity_y" )

                    // X軸の移動距離が大きすぎる場合
                    if(Math.abs(e1.x - e2.x) > SWIPE_MAX_OFF_PATH){
                        Log.d("MyView_onFling","横の移動距離が大きすぎます")

                        //開始位置から終了位置の移動距離が指定値より大きい
                        //Y軸の移動速度が指定値より大きい
                    }else if(e2.y - e1.y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY){
                        Log.d("onFling","上から下へ")
                        LayerService.context?.run{
                            val intent = Intent(this,LayerService::class.java)
                                    .setAction(LayerService.ACTION_SUSPEND)
                            startService(intent)
                        }

                        // 終了位置から開始位置の移動距離が指定値より大きい
                        // Y軸の移動速度が指定値より大きい
                    }else if(e1.y - e2.y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY){
                        Log.d("onFling","下から上")
                        LayerService.context?.run{
                            val intent = Intent(this,LayerService::class.java)
                                    .setAction(LayerService.ACTION_SUSPEND)
                            startService(intent)
                        }
                    }
                } catch (e: Exception) {

                }
                return false
            }
            override fun onDown(event: MotionEvent):Boolean{
                initial = params.position - event.position
                return true
            }
        })
    }
}