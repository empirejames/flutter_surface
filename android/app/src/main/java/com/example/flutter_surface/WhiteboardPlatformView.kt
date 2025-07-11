package com.example.flutter_surface

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class WhiteboardPlatformView(context: Context, messenger: BinaryMessenger, id: Int) :
    SurfaceView(context), SurfaceHolder.Callback, PlatformView, MethodChannel.MethodCallHandler {

    private val channel = MethodChannel(messenger, "whiteboard_$id")
    private var drawPaint = Paint().apply {
        color = 0xFF000000.toInt()
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val erasePaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        strokeWidth = 32f
        style = Paint.Style.STROKE
    }
    private var useErase = false

    private lateinit var bitmap: Bitmap
    private lateinit var canvasBuffer: Canvas
    private var path = Path()

    init {
        holder.addCallback(this)
        channel.setMethodCallHandler(this)
    }

    override fun getView() = this
    override fun dispose() {}

    override fun surfaceCreated(holder: SurfaceHolder) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvasBuffer = Canvas(bitmap)
        drawCanvas()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                canvasBuffer.drawPath(path, if (useErase) erasePaint else drawPaint)
                path.reset()
            }
        }
        drawCanvas()
        return true
    }

    private fun drawCanvas() {
        val canvas = holder.lockCanvas()
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawPath(path, if (useErase) erasePaint else drawPaint)
        holder.unlockCanvasAndPost(canvas)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "setMode") {
            val mode = call.argument<String>("mode")
            useErase = mode == "erase"
            result.success(null)
        } else {
            result.notImplemented()
        }
    }
}
