package com.amplez.yoo_hoo

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextSwitcher
import android.widget.Toast
import com.rengwuxian.materialedittext.MaterialEditText

private const  val MAX_PHRASES_CHAR_LENGTH: Int = 150
class MainActivity : AppCompatActivity() {

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    private var mPermissionToRecord = false

    private lateinit var sp: SharedPrefs
    private lateinit var mService: WebSocketService
    private var previousHeardPhrase :String = ""
    private var newPhraseFetcherHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SharedPrefs.getInstance().initSharedPrefs(this)
        sp = SharedPrefs.getInstance()
        initEdittextLiveValidation()
        initPhraseRefresher()

    }

    private lateinit  var phraseTS: TextSwitcher

    private fun initPhraseRefresher() {
        phraseTS = findViewById<TextSwitcher>(R.id.phraseTS)
        phraseTS.setInAnimation(applicationContext, android.R.anim.fade_in)
        phraseTS.setOutAnimation(applicationContext, android.R.anim.fade_out)
        newPhraseFetcherHandler.removeCallbacks(wordsFetcherRunnable)
        newPhraseFetcherHandler.post(wordsFetcherRunnable)

    }


    private val wordsFetcherRunnable = object: Runnable {
        override fun run() {

            try {
                if (!mBound)
                    return

                val latestPhraseHeard = mService.getLatestHeardPhrase()
                Log.d("zvi","latestPhraseHeard : $latestPhraseHeard ")
                Log.d("zvi","previousHeardPhrase: $previousHeardPhrase")
                if (latestPhraseHeard == previousHeardPhrase)
                    return
                previousHeardPhrase = latestPhraseHeard
                phraseTS.setText(previousHeardPhrase)
            }finally {

                newPhraseFetcherHandler.postDelayed(this,1500)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        setEnv()
    }


    private fun setEnv() {
        val isServiceRunnig = isServiceRunning()
        userCustomPhrasesETEnabled(!isServiceRunnig)
        setActivateSwitch(isServiceRunnig)
        tryBindService(isServiceRunnig)
        if(isServiceRunnig){
            resetPhraseDisplayer()
        }

    }

    private fun tryBindService(serviceRunnig: Boolean) {
        if(!serviceRunnig)
            return
        Intent(this, WebSocketService::class.java).also { intent ->
            bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT)
        }
    }

    private var mBound: Boolean = false
    /** Defines callbacks for service binding, passed to bindService()  */
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as WebSocketService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(mConnection)
        mBound = false
    }

    private var activationEt: MaterialEditText? = null

    private var deactivationEt: MaterialEditText? = null

    private fun initEdittextLiveValidation() {
        activationEt = findViewById<MaterialEditText>(R.id.activationET)
        deactivationEt = findViewById<MaterialEditText>(R.id.deactivationET)

        activationEt!!.addTextChangedListener(activationTextWatcherValidator)
        deactivationEt!!.addTextChangedListener(deactivationTextWatcherValidator)
        loadPhrasesFromSp()
    }

    private fun loadPhrasesFromSp() {
        val strActivationList: String = getStrFromPhraseSet(sp.activateWordsSet)
        activationEt?.setText(strActivationList)
        val strDeactivationList: String = getStrFromPhraseSet(sp.deactivateWordsSet)
        deactivationEt?.setText(strDeactivationList)
    }

    private fun getStrFromPhraseSet(activateWordsSet: Set<String>): String {
        var result = ""
        for (w in activateWordsSet) {
            result += "$w, "
        }
        return result.substring(0, result.length - 2)
    }

    private val activationTextWatcherValidator = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            var validatedText = phraseParser(s)
            if(validatedText.length>MAX_PHRASES_CHAR_LENGTH){
                validatedText = validatedText.substring(0,MAX_PHRASES_CHAR_LENGTH)
            }
            if (validatedText != s.toString()) {
                activationEt!!.setText(validatedText)
                activationEt!!.setSelection(validatedText.length)
            }
            sp.setActivateWords(validatedText)

        }

    }

    private fun phraseParser(phrae: CharSequence): String {
        return phrae.toString().replace("[^A-Za-z ,-]".toRegex(), "")
    }


    private val deactivationTextWatcherValidator = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            var validatedText = phraseParser(s)
            if(validatedText.length>MAX_PHRASES_CHAR_LENGTH){
                validatedText = validatedText.substring(0,MAX_PHRASES_CHAR_LENGTH)
            }
            if (validatedText != s.toString()) {
                deactivationEt!!.setText(validatedText)
                deactivationEt!!.setSelection(validatedText.length)
            }
            sp.setDeactivateWords(validatedText)
        }

    }

    private fun isServiceRunning(): Boolean {
        return ServiceTools.isServiceRunning(this@MainActivity.applicationContext, WebSocketService::class.java)
    }

    private fun setActivateSwitch(isServiceRunning: Boolean) {

        findViewById<Switch>(R.id.switch1).isChecked = isServiceRunning
        findViewById<Switch>(R.id.switch1).setOnCheckedChangeListener { compoundButton: CompoundButton, isActive: Boolean ->
            if (isActive) {
                startYooHooService()
            } else {
                killYooHooService()
            }
        }

    }
/*
    private fun cancelAlarmManger() {
        var alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var alarmIntent = Intent(this, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, 0)
        }
        alarmMgr?.cancel(alarmIntent)
    }*/


    private fun killYooHooService() {
        val i = Intent(this, WebSocketService::class.java)
        stopService(i)
        Toast.makeText(this, getString(R.string.yoo_hoo_service_killed), Toast.LENGTH_SHORT).show()
//        cancelAlarmManger()
        userCustomPhrasesETEnabled(true)
        resetPhraseDisplayer()
    }

    private fun startYooHooService() {
        if (mPermissionToRecord) {
            if(!WebSocketService.safeToStartService){
                preventServiceStart()
                return
            }
            loadPhrasesFromSp()

            userCustomPhrasesETEnabled(false)
            findViewById<Switch>(R.id.switch1).isChecked = true
            val i = Intent(this, WebSocketService::class.java)
            Toast.makeText(this, getString(R.string.yoo_hoo_service_started), Toast.LENGTH_SHORT).show()
            ContextCompat.startForegroundService(this, i)
            resetPhraseDisplayer()
            tryBindService(true)
        } else {
            askRecMic()
            findViewById<Switch>(R.id.switch1).isChecked = false
        }

    }

    private fun preventServiceStart() {
        Toast.makeText(this, getString(R.string.cant_start_service_yet), Toast.LENGTH_SHORT).show()
        findViewById<Switch>(R.id.switch1).isChecked = false

    }

    private fun resetPhraseDisplayer() {
        previousHeardPhrase = ""
        phraseTS?.setText(previousHeardPhrase)

    }

    private fun userCustomPhrasesETEnabled(enable: Boolean) {
        activationEt?.isEnabled = enable
        deactivationEt?.isEnabled = enable
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            mPermissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
            startYooHooService()
        }

        // bail out if audio recording is not available
        if (!mPermissionToRecord) {
            finish()
        }
    }

    fun askRecMic() {
        // get permissions
        // get permissions
        ActivityCompat.requestPermissions(
            this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION
        )
    }
}
