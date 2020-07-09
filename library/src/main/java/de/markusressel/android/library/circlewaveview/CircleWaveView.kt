/*
 * Copyright (c) 2016 Markus Ressel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.markusressel.android.library.circlewaveview

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.min

/**
 * Simple "wave" like circle indicator view
 *
 *
 * Created by Markus on 08.11.2016.
 */
class CircleWaveView : View {

    /**
     * The start diameter
     */
    var startDiameter = 0f
        set(value) {
            field = value
            if (isInitialized) {
                sizeAnimators.forEach {
                    it.setFloatValues(field, endDiameter)
                }
            }
        }

    /**
     * The end diameter
     */
    var endDiameter = 0f
        set(value) {
            field = value
            if (isInitialized) {
                sizeAnimators.forEach {
                    it.setFloatValues(startDiameter, field)
                }
            }
        }

    /**
     * The start color
     */
    @ColorInt
    var startColor = 0
        set(value) {
            field = value
            if (isInitialized) {
                colorAnimators.forEach {
                    it.setObjectValues(field, endColor)
                }
            }
        }

    /**
     * The end color
     */
    @ColorInt
    var endColor = 0
        set(value) {
            field = value
            if (isInitialized) {
                colorAnimators.forEach {
                    it.setObjectValues(startColor, field)
                }
            }
        }

    /**
     * The stroke width
     */
    var strokeWidth = 0f
        set(value) {
            field = value
            if (isInitialized) {
                paints.forEach {
                    it.strokeWidth = strokeWidth
                }
            }
        }

    /**
     * The animation duration
     */
    var duration = 0
        set(value) {
            field = value
            if (isInitialized) {
                (colorAnimators + sizeAnimators).forEach {
                    it.duration = duration.toLong()
                }
            }
        }

    /**
     * The delay in milliseconds between spawning new waves
     */
    var delayBetweenWaves = 0
        set(value) {
            field = value
            reinitialize()
        }

    /**
     * The number of waves
     */
    var waveCount = 0
        set(value) {
            cleanupAnimators()
            field = value
            init()
            startAnimators()
        }

    /**
     * Animation interpolator for both color and size
     */
    var interpolator: Interpolator = FastOutSlowInInterpolator()
        set(value) {
            field = value
            if (isInitialized) {
                reinitialize()
            }
        }

    private lateinit var currentDiameters: FloatArray
    private val paints = mutableListOf<Paint>()
    private val colorAnimators = mutableListOf<ValueAnimator>()
    private val sizeAnimators = mutableListOf<ValueAnimator>()
    private var isInitialized = false

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        readArguments(context, attrs)
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        readArguments(context, attrs)
    }

    private fun readArguments(context: Context, attrs: AttributeSet?) {
        // read XML attributes
        val a = context.theme
                .obtainStyledAttributes(attrs, R.styleable.CircleWaveView, 0, 0)
        try {
            val defaultColor = getAccentColor(context)
            startDiameter = a.getDimensionPixelSize(R.styleable.CircleWaveView_cwav_startDiameter,
                    0).toFloat()
            endDiameter = a.getDimensionPixelSize(R.styleable.CircleWaveView_cwav_targetDiameter,
                    -1).toFloat()
            startColor = a.getColor(R.styleable.CircleWaveView_cwav_startColor, defaultColor)
            @ColorInt val defaultEndColor = Color.argb(0,
                    Color.red(startColor),
                    Color.green(startColor),
                    Color.blue(startColor))
            endColor = a.getColor(R.styleable.CircleWaveView_cwav_endColor, defaultEndColor)
            strokeWidth = a.getColor(R.styleable.CircleWaveView_cwav_strokeWidth,
                    DEFAULT_STROKE_WIDTH).toFloat()
            duration = a.getInt(R.styleable.CircleWaveView_cwav_durationMilliseconds,
                    DEFAULT_DURATION_MILLISECONDS)
            delayBetweenWaves = a.getInt(R.styleable.CircleWaveView_cwav_delayMillisecondsBetweenWaves,
                    -1)
            waveCount = a.getInt(R.styleable.CircleWaveView_cwav_waveCount,
                    DEFAULT_WAVE_COUNT)
        } finally {
            a.recycle()
        }
    }

    private fun getAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
        val color = a.getColor(0, DEFAULT_COLOR)
        a.recycle()
        return color
    }

    @Synchronized
    private fun reinitialize() {
        cleanupAnimators()
        init()
        startAnimators()
    }

    @Synchronized
    private fun init() {
        currentDiameters = FloatArray(waveCount)
        cleanupAnimators()
        for (i in 0 until waveCount) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = startColor
                style = Paint.Style.STROKE
                strokeWidth = this@CircleWaveView.strokeWidth
                paints.add(this)
            }

            ValueAnimator.ofFloat(startDiameter, endDiameter).apply {
                duration = this@CircleWaveView.duration.toLong()
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = this@CircleWaveView.interpolator
                addUpdateListener { animation ->
                    currentDiameters[i] = animation.animatedValue as Float

                    // we only need to rerender the view if the first animator updates, as all animators update at the same speed
                    if (i == 0) {
                        invalidate()
                    }
                }
                sizeAnimators.add(this)
            }

            ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor).apply {
                duration = this@CircleWaveView.duration.toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = this@CircleWaveView.interpolator
                addUpdateListener { animation ->
                    this@CircleWaveView.paints[i].color = animation.animatedValue as Int
                }
                colorAnimators.add(this)
            }

            val delay = when (delayBetweenWaves) {
                -1 -> i * (duration / waveCount)
                else -> i * delayBetweenWaves
            }
            sizeAnimators[i].startDelay = delay.toLong()
            colorAnimators[i].startDelay = delay.toLong()
        }

        isInitialized = true
    }

    private fun startAnimators() {
        if (isInitialized) {
            (sizeAnimators + colorAnimators).forEach {
                it.start()
            }
        }
    }

    @TargetApi(19)
    private fun pauseAnimators() {
        if (isInitialized) {
            (sizeAnimators + colorAnimators).forEach {
                it.pause()
            }
        }
    }

    @TargetApi(19)
    private fun resumeAnimators() {
        if (isInitialized) {
            (sizeAnimators + colorAnimators).forEach {
                it.resume()
            }
        }
    }

    private fun cancelAnimators() {
        if (isInitialized) {
            (sizeAnimators + colorAnimators).forEach {
                it.removeAllUpdateListeners()
                it.cancel()
            }
        }
    }

    private fun cleanupAnimators() {
        if (isInitialized) {
            cancelAnimators()
            paints.clear()
            sizeAnimators.clear()
            colorAnimators.clear()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val viewWidth = endDiameter.toInt() + (strokeWidth / 2).toInt() + paddingLeft + paddingRight
        val viewHeight = endDiameter.toInt() + (strokeWidth / 2).toInt() + paddingTop + paddingBottom
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // Measure Width
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize // Must be this size
            MeasureSpec.AT_MOST -> min(viewWidth, widthSize) // Can't be bigger than...
            else -> viewWidth // Be whatever you want
        }

        // Measure Height
        val height = when {
            heightMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.EXACTLY -> heightSize // Must be this size
            heightMode == MeasureSpec.AT_MOST -> min(viewHeight, heightSize) // Can't be bigger than...
            else -> viewHeight // Be whatever you want
        }

        setMeasuredDimension(width, height)
        if (endDiameter == -1f) {
            endDiameter = min(width, height).toFloat()
        }
        reinitialize()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = width / 2.toFloat()
        val y = height / 2.toFloat()
        for (i in 0 until waveCount) {
            canvas.drawCircle(x, y, currentDiameters[i] / 2, paints[i])
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                resumeAnimators()
            } else {
                startAnimators()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                pauseAnimators()
            } else {
                cancelAnimators()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cleanupAnimators()
    }

    companion object {
        private const val TAG = "CircleWaveAlertView"

        @ColorInt
        private val DEFAULT_COLOR = Color.BLUE
        private const val DEFAULT_STROKE_WIDTH = 3
        private const val DEFAULT_DURATION_MILLISECONDS = 3000
        private const val DEFAULT_WAVE_COUNT = 3
    }
}