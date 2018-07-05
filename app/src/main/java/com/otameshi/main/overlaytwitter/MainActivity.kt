package com.otameshi.main.overlaytwitter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = "MainActivity"
        private val REQUEST_OVERLAY_PERMISSION = 1
    }

    private var enabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener{

            if(hasOverlayPermission()){
                val intent = Intent(this,LayerService::class.java)
                        .setAction(LayerService.ACTION_START)
                        .putExtra("webViewID",webView.id)
                startService(intent)
            }else{
                requestOverlayPermission(REQUEST_OVERLAY_PERMISSION)
            }

        }

        button2.setOnClickListener {
            if(enabled && hasOverlayPermission()){
                val intent = Intent(this,LayerService::class.java)
                        .setAction(LayerService.ACTION_STOP)
                startService(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQUEST_OVERLAY_PERMISSION -> Log.d(TAG,"enable overlay permission")
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        enabled = false
        return super.onTouchEvent(event)
    }
}
