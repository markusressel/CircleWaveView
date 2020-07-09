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
package de.markusressel.android.circlewaveview

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.markusressel.android.library.circlewaveview.CircleWaveView

class MainActivity : AppCompatActivity() {
    private lateinit var circleWaveView: CircleWaveView
    private lateinit var broadcastReceiver: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        circleWaveView = findViewById<View>(R.id.circleWaveAlertView) as CircleWaveView

        // Load Settings view
        fragmentManager.beginTransaction()
                .replace(R.id.preferenceFrame, SettingsFragment.newInstance())
                .commit()


        // load settings
        initFromPreferenceValues()

        // this receiver will update the view if a preference has changed
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (SettingsFragment.INTENT_ACTION_PREFERENCE_CHANGED == intent.action) {
                    val key = intent.getStringExtra(SettingsFragment.KEY_PREFERENCE_KEY)
                    initFromPreferenceValues()

                    //                    if (getString(R.string.key_activeIndicatorFillColor).equals(key)) {
                    //                        @ColorInt int activeIndicatorColorFill = PreferencesHelper.getColor(getApplicationContext(), R.string.key_activeIndicatorFillColor, getResources().getColor(R.color.default_value_activeIndicatorFillColor));
                    //                        pageIndicatorView.setActiveIndicatorFillColor(activeIndicatorColorFill);
                    //                    } else if (getString(R.string.key_activeIndicatorStrokeColor).equals(key)) {
                    //                        @ColorInt int activeIndicatorColorStroke = PreferencesHelper.getColor(getApplicationContext(), R.string.key_activeIndicatorStrokeColor, getResources().getColor(R.color.default_value_activeIndicatorStrokeColor));
                    //                        pageIndicatorView.setActiveIndicatorStrokeColor(activeIndicatorColorStroke);
                    //                    } else if (getString(R.string.key_activeIndicatorStrokeWidth).equals(key)) {
                    //                        float activeIndicatorStrokeWidth = PreferencesHelper.getDimen(getApplicationContext(), R.string.key_activeIndicatorStrokeWidth, R.dimen.default_value_activeIndicatorStrokeWidth);
                    //                        pageIndicatorView.setActiveIndicatorStrokeWidth(Math.round(pxFromDp(getApplicationContext(), activeIndicatorStrokeWidth)));
                    //                    } else if (getString(R.string.key_activeIndicatorFillSize).equals(key)) {
                    //                        float activeIndicatorSize = PreferencesHelper.getDimen(getApplicationContext(), R.string.key_activeIndicatorFillSize, R.dimen.default_value_activeIndicatorFillSize);
                    //                        pageIndicatorView.setActiveIndicatorSize(Math.round(pxFromDp(getApplicationContext(), activeIndicatorSize)));
                    //                    } else if (getString(R.string.key_inactiveIndicatorFillColor).equals(key)) {
                    //                        @ColorInt int inactiveIndicatorColorFill = PreferencesHelper.getColor(getApplicationContext(), R.string.key_inactiveIndicatorFillColor, getResources().getColor(R.color.default_value_inactiveIndicatorFillColor));
                    //                        pageIndicatorView.setInactiveIndicatorFillColor(inactiveIndicatorColorFill);
                    //                    } else if (getString(R.string.key_inactiveIndicatorStrokeColor).equals(key)) {
                    //                        @ColorInt int inactiveIndicatorColorStroke = PreferencesHelper.getColor(getApplicationContext(), R.string.key_inactiveIndicatorStrokeColor, getResources().getColor(R.color.default_value_inactiveIndicatorStrokeColor));
                    //                        pageIndicatorView.setInactiveIndicatorStrokeColor(inactiveIndicatorColorStroke);
                    //                    } else if (getString(R.string.key_inactiveIndicatorStrokeWidth).equals(key)) {
                    //                        float inactiveIndicatorStrokeWidth = PreferencesHelper.getDimen(getApplicationContext(), R.string.key_inactiveIndicatorStrokeWidth, R.dimen.default_value_inactiveIndicatorStrokeWidth);
                    //                        pageIndicatorView.setInactiveIndicatorStrokeWidth(Math.round(pxFromDp(getApplicationContext(), inactiveIndicatorStrokeWidth)));
                    //                    } else if (getString(R.string.key_inactiveIndicatorFillSize).equals(key)) {
                    //                        float inactiveIndicatorSize = PreferencesHelper.getDimen(getApplicationContext(), R.string.key_inactiveIndicatorFillSize, R.dimen.default_value_inactiveIndicatorFillSize);
                    //                        pageIndicatorView.setInactiveIndicatorSize(Math.round(pxFromDp(getApplicationContext(), inactiveIndicatorSize)));
                    //
                    //                    } else if (getString(R.string.key_indicatorGap).equals(key)) {
                    //                        float indicatorGap = PreferencesHelper.getDimen(getApplicationContext(), R.string.key_indicatorGap, R.dimen.default_value_indicatorGap);
                    //                        pageIndicatorView.setIndicatorGap(Math.round(pxFromDp(getApplicationContext(), indicatorGap)));
                    //                    } else if (getString(R.string.key_initialPageIndex).equals(key)) {
                    //                        int initialPageIndex = PreferencesHelper.getInteger(getApplicationContext(), R.string.key_initialPageIndex, R.integer.default_value_initialPageIndex);
                    //                        // TODO
                    //                    } else if (getString(R.string.key_pageCount).equals(key)) {
                    //                        int pageCount = PreferencesHelper.getInteger(getApplicationContext(), R.string.key_pageCount, R.integer.default_value_pageCount);
                    //                        customTabAdapter.setCount(pageCount);
                    //                        pageIndicatorView.setPageCount(pageCount);
                    //                    }
                }
            }
        }
    }

    private fun initFromPreferenceValues() {
        @ColorInt val startColor = PreferencesHelper.getColor(applicationContext,
                R.string.key_startColor,
                resources.getColor(R.color.default_value_startColor))
        @ColorInt val endColor = PreferencesHelper.getColor(applicationContext,
                R.string.key_endColor,
                resources.getColor(R.color.default_value_endColor))
        val startDiameter = PreferencesHelper.getDimen(applicationContext,
                R.string.key_startDiameter,
                R.dimen.default_value_startDiameter)
        val targetDiameter = PreferencesHelper.getDimen(applicationContext,
                R.string.key_targetDiameter,
                R.dimen.default_value_targetDiameter)
        val strokeWidth = PreferencesHelper.getDimen(applicationContext,
                R.string.key_strokeWidth,
                R.dimen.default_value_strokeWidth)
        val delayBetweenWaves = PreferencesHelper.getInteger(applicationContext,
                R.string.key_delayBetweenWaves,
                R.integer.default_value_delayBetweenWaves)
        val duration = PreferencesHelper.getInteger(applicationContext,
                R.string.key_duration,
                R.integer.default_value_duration)
        val waveCount = PreferencesHelper.getInteger(applicationContext,
                R.string.key_waveCount,
                R.integer.default_value_waveCount)
        circleWaveView.startColor = startColor
        circleWaveView.endColor = endColor
        circleWaveView.startDiameter = pxFromDp(this, startDiameter)
        circleWaveView.endDiameter = pxFromDp(this, targetDiameter)
        circleWaveView.strokeWidth = pxFromDp(this, strokeWidth)
        circleWaveView.delayBetweenWaves = delayBetweenWaves
        circleWaveView.duration = duration
        circleWaveView.waveCount = waveCount
        circleWaveView.interpolator = FastOutSlowInInterpolator()
    }

    public override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(SettingsFragment.INTENT_ACTION_PREFERENCE_CHANGED)
        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(broadcastReceiver, intentFilter)
    }

    public override fun onStop() {
        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(broadcastReceiver)
        super.onStop()
    }

    companion object {
        private fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }
    }
}