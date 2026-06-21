package digital.euforia.app.ui.onboardingV3.pager

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RawRes
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class BloomVideoView(context: Context) : TextureView(context), TextureView.SurfaceTextureListener {

    @RawRes
    private var videoRes: Int = 0
    private var renderThread: BloomRenderThread? = null

    init {
        isOpaque = false
        surfaceTextureListener = this
    }

    fun setVideoResource(@RawRes resId: Int) {
        if (videoRes == resId) return
        videoRes = resId
        renderThread?.setVideoResource(resId)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        renderThread = BloomRenderThread(context.applicationContext, surface, videoRes).also { it.start() }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderThread?.shutdown()
        renderThread = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

    override fun onDetachedFromWindow() {
        renderThread?.shutdown()
        renderThread = null
        super.onDetachedFromWindow()
    }
}

private class BloomRenderThread(
    private val context: Context,
    private val outputSurfaceTexture: SurfaceTexture,
    @param:RawRes private var videoRes: Int,
) : Thread("BloomVideoRenderer") {

    private val isRunning = AtomicBoolean(true)
    private val frameAvailable = AtomicBoolean(false)

    private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var outputSurface: Surface? = null

    private var videoTexture: SurfaceTexture? = null
    private var videoSurface: Surface? = null
    private var mediaPlayer: MediaPlayer? = null
    private var program = 0
    private var textureId = 0
    private val textureMatrix = FloatArray(16)

    fun setVideoResource(@RawRes resId: Int) {
        videoRes = resId
    }

    fun shutdown() {
        isRunning.set(false)
        interrupt()
    }

    override fun run() {
        try {
            setupEgl()
            setupGl()
            setupVideo()

            while (isRunning.get()) {
                if (frameAvailable.getAndSet(false)) {
                    videoTexture?.updateTexImage()
                    videoTexture?.getTransformMatrix(textureMatrix)
                }
                draw()
                sleep(16L)
            }
        } catch (_: InterruptedException) {
            // Normal shutdown.
        } finally {
            release()
        }
    }

    private fun setupEgl() {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        EGL14.eglInitialize(display, null, 0, null, 0)

        val configAttributes = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 0,
            EGL14.EGL_STENCIL_SIZE, 0,
            EGL14.EGL_NONE,
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val configCount = IntArray(1)
        EGL14.eglChooseConfig(display, configAttributes, 0, configs, 0, 1, configCount, 0)

        val contextAttributes = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttributes, 0)

        outputSurface = Surface(outputSurfaceTexture)
        val surfaceAttributes = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(display, configs[0], outputSurface, surfaceAttributes, 0)
        EGL14.eglMakeCurrent(display, eglSurface, eglSurface, eglContext)
    }

    private fun setupGl() {
        android.opengl.Matrix.setIdentityM(textureMatrix, 0)
        textureId = createExternalTexture()
        videoTexture = SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener { frameAvailable.set(true) }
        }
        videoSurface = Surface(videoTexture)
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        GLES20.glClearColor(0f, 0f, 0f, 0f)
    }

    private fun setupVideo() {
        if (videoRes == 0) return
        val afd = context.resources.openRawResourceFd(videoRes)
        mediaPlayer = MediaPlayer().apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            setSurface(videoSurface)
            isLooping = false
            setVolume(0f, 0f)
            setOnPreparedListener { it.start() }
            prepareAsync()
        }
    }

    private fun draw() {
        GLES20.glViewport(0, 0, surfaceWidth(), surfaceHeight())
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        val textureMatrixHandle = GLES20.glGetUniformLocation(program, "uTextureMatrix")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, FULL_RECTANGLE_BUF)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, TEXTURE_COORD_BUF)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(textureHandle, 0)
        GLES20.glUniformMatrix4fv(textureMatrixHandle, 1, false, textureMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        EGL14.eglSwapBuffers(display, eglSurface)
    }

    private fun surfaceWidth(): Int {
        val value = IntArray(1)
        EGL14.eglQuerySurface(display, eglSurface, EGL14.EGL_WIDTH, value, 0)
        return value[0]
    }

    private fun surfaceHeight(): Int {
        val value = IntArray(1)
        EGL14.eglQuerySurface(display, eglSurface, EGL14.EGL_HEIGHT, value, 0)
        return value[0]
    }

    private fun release() {
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        runCatching { videoSurface?.release() }
        videoSurface = null
        runCatching { videoTexture?.release() }
        videoTexture = null
        if (program != 0) GLES20.glDeleteProgram(program)
        if (textureId != 0) GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        if (display != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(display, eglSurface)
            EGL14.eglDestroyContext(display, eglContext)
            EGL14.eglTerminate(display)
        }
        runCatching { outputSurface?.release() }
        outputSurface = null
    }

    private fun createExternalTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        return textures[0]
    }

    private fun createProgram(vertexShaderSource: String, fragmentShaderSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
        return GLES20.glCreateProgram().also { program ->
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
        }
    }

    private fun loadShader(type: Int, shaderSource: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderSource)
            GLES20.glCompileShader(shader)
        }
    }

    companion object {
        private val FULL_RECTANGLE_BUF = floatBufferOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
        )

        private val TEXTURE_COORD_BUF = floatBufferOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
        )

        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            uniform mat4 uTextureMatrix;
            varying vec2 vTexCoord;
            varying vec2 vMaskCoord;
            void main() {
                gl_Position = aPosition;
                vec2 transformed = (uTextureMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;
                vec2 videoCoord = vec2(transformed.x, 1.0 - transformed.y);
                vTexCoord = vec2(videoCoord.x, 0.2 + videoCoord.y * 0.6);
                vMaskCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES uTexture;
            varying vec2 vTexCoord;
            varying vec2 vMaskCoord;
            void main() {
                vec4 src = texture2D(uTexture, vTexCoord);
                float brightness = max(max(src.r, src.g), src.b);
                float keyAlpha = smoothstep(0.025, 0.18, brightness);
                float distanceFromCenter = distance(vMaskCoord, vec2(0.5, 0.5));
                float softMask = 1.0 - smoothstep(0.38, 0.50, distanceFromCenter);
                float topFade = smoothstep(0.00, 0.28, vMaskCoord.y);
                float bottomFade = 1.0 - smoothstep(0.72, 1.0, vMaskCoord.y);
                float edgeFade = softMask * topFade * bottomFade;
                float alpha = keyAlpha * edgeFade;
                vec3 saturated = mix(vec3(dot(src.rgb, vec3(0.299, 0.587, 0.114))), src.rgb, 1.1);
                saturated *= mix(0.35, 1.0, edgeFade);
                gl_FragColor = vec4(saturated, alpha);
            }
        """
    }
}

private fun floatBufferOf(vararg values: Float): FloatBuffer {
    return ByteBuffer
        .allocateDirect(values.size * java.lang.Float.BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(values)
            position(0)
        }
}
