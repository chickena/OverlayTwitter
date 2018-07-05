package com.otameshi.main.overlaytwitter

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View



class ViewTest : View {
    constructor(context: Context) : super(context) {
        mContext = context
        mGestureDetector = GestureDetector(mContext, mSimpleOnGestureListener)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        mGestureDetector = GestureDetector(mContext, mSimpleOnGestureListener)

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context,attrs,defStyleAttr){
        mContext = context
        mGestureDetector = GestureDetector(mContext, mSimpleOnGestureListener)
        mScaleGestureDetector = ScaleGestureDetector(mContext,mScaleGestureListener)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context,attrs, defStyleAttr, defStyleRes){
        mContext = context
        mGestureDetector = GestureDetector(mContext, mSimpleOnGestureListener)
    }
    private var mContext : Context? = null
    var mGestureDetector : GestureDetector? = null
    var mScaleGestureDetector:ScaleGestureDetector? = null

    companion object {
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_MAX_OFF_PATH = 250
        private const val SWIPE_THRESHOLD_VELOCITY = 200
    }
    fun setOnGestureListener(listener:GestureDetector.SimpleOnGestureListener?,listener2:ScaleGestureDetector.SimpleOnScaleGestureListener?){
        listener?.run{
            //引数がnullでなければそれを使う
            mGestureDetector = GestureDetector(mContext,listener)
            mScaleGestureDetector = ScaleGestureDetector(mContext,listener2)
            return
        }
        //第二引数にnullが許されないのでデフォルトで作ってあるリスナーを使う
        mGestureDetector = GestureDetector(mContext,mSimpleOnGestureListener)
        mScaleGestureDetector = ScaleGestureDetector(mContext,mScaleGestureListener)
    }

    private val mSimpleOnGestureListener = object :GestureDetector.SimpleOnGestureListener(){
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
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

                    // 終了位置から開始位置の移動距離が指定値より大きい
                    // Y軸の移動速度が指定値より大きい
                }else if(e1.y - e2.y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY){
                    Log.d("onFling","下から上")

                }
            }catch(e:Exception){

            }
            return false
        }
        // 長押し
        override fun onLongPress(e: MotionEvent?) {
            Log.d("MyView", "onLongPress()")
        }

        override fun onDown(e: MotionEvent?): Boolean {
            Log.d("MyView", "onDown()")
            return true
        }
    }

    private val mScaleGestureListener = object :ScaleGestureDetector.SimpleOnScaleGestureListener(){
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScale = 1.0F
            mScale *= detector.scaleFactor
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            Log.d("MyView", "MyView.onScaleBegin")
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            Log.d("MyView", "MyView.onScaleEnd")
        }
    }

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        return mGestureDetector!!.onTouchEvent(event)
//    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (event!!.pointerCount === 1) mGestureDetector!!.onTouchEvent(event) else mScaleGestureDetector!!.onTouchEvent(event)
    }
}