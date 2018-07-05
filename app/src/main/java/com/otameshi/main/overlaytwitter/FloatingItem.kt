package com.otameshi.main.overlaytwitter

import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.*
import com.otameshi.main.overlaytwitter.R.id.floatingWebView
import kotlinx.android.synthetic.main.overlay.view.*

class FloatingItem(val windowManager: WindowManager, val view: View) {
    companion object {
        private val TAG = FloatingItem::class.qualifiedName
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
                    val parent = view.parent as ViewGroup
                    if(parent !=null){
                        parent.removeAllViews()
                    }
                    windowManager.addView(view, params)
                } else {
                    windowManager.removeView(view)
                }
            }
        }

    private var initial: Position? = null

    init {
        view.floatingWebView.setOnTouchListener { view, event ->
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
        view.floatingWebView.setOnClickListener {
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
}