package com.otameshi.main.overlaytwitter

import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
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

//    private fun setOnGestureListener(){
//        view.viewTest.setOnGestureListener(object :GestureDetector.SimpleOnGestureListener(){
//            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
//                Log.d(TAG,"onFling()")
//                try{
//                    val distance_y = Math.abs(e1.y - e2.y)
//                }
//                return super.onFling(e1, e2, velocityX, velocityY)
//            }
//        })
//    }
}