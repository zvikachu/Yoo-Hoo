package com.amplez.yoo_hoo

import android.Manifest
import android.content.Context
import android.widget.TextSwitcher

class SpeechHandler(private val context: Context){

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    private var mPermissionToRecord = false
    private var mAudioEmitter: AudioEmitter? = null
    private lateinit var mTextView: TextSwitcher



}
