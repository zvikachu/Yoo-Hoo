package com.amplez.yoo_hoo

/*
 *
 *
 *           Intent i = new Intent(context, AlarmService.class);
 *         ContextCompat.startForegroundService(context,i);

 *
 *
 *
 * */

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.amplez.yoo_hoo.NotificationController.CHANNEL_ID
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


private const val TAG = "zvi yoo-hoo service"
private const val WAIT_TIME_BETWEEN_RESTARTS :Long= 1000*8

class WebSocketService : Service() {


    companion object {
        private const val NOTIFICATION_ID = 13397
        private const val YOO_HOO_ACTIVE_VOL_LEVEL = 2
        var safeToStartService = true

    }

    private var notifBuilt: Boolean = false
    private var mAudioEmitter: AudioEmitter? = null

    private var started = false

    private var refreshCount: Int = 0
    private var mSpeechClient: SpeechClient? = null
    private var requestStream: ApiStreamObserver<StreamingRecognizeRequest>? = null
    private var isFirstRequest = AtomicBoolean(true)
    private var latestHeardPhrase = ""
    // Binder given to clients
    private val mBinder = LocalBinder()

    fun getLatestHeardPhrase(): String {
        return latestHeardPhrase
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): WebSocketService = this@WebSocketService
    }


    private fun removeTraces(shutdown: Boolean = false) {
        mAudioEmitter?.stop()
        mSpeechClient?.streamingRecognizeCallable()?.bidiStreamingCall(null)
        if (shutdown) {
            mSpeechClient?.awaitTermination(5, TimeUnit.SECONDS)
            mSpeechClient = null
        } else {
            mSpeechClient?.shutdown()

        }
        mAudioEmitter = null
        requestStream = null
        isFirstRequest.set(true)
        started = false
    }

//    private var restartOngoing: Boolean = false

    private fun restartSelf() {
        Thread {
            if(!safeToStartService)
                return@Thread
            coolDownRestart()
//        if(restartOngoing)return
//        restartOngoing = true
            Log.d(TAG, "restart requested")
            removeTraces()
            init()
            Log.d(TAG, "restart performed")
        }.start()
    }

    private var lastRestartTimeMillis =  0L

    private fun coolDownRestart() {
        val restartsDelta = System.currentTimeMillis()-lastRestartTimeMillis
        if(restartsDelta < WAIT_TIME_BETWEEN_RESTARTS){
            val sleepTime = WAIT_TIME_BETWEEN_RESTARTS-restartsDelta
            Log.d(TAG,"sleeping for $sleepTime before restarting service")
            Thread.sleep(sleepTime)
        }
        lastRestartTimeMillis =  System.currentTimeMillis()

    }

    override fun onDestroy() {
        Thread {
            safeToStartService = false
            removeTraces(true)
            super.onDestroy()
            safeToStartService = true
        }.start()
    }


    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }


    private fun initNotification() {
        if (notifBuilt) return
        notifBuilt = true
        NotificationController().createNotificationChannel(this)
        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)

            .setSmallIcon(R.drawable.ic_sync_black_24dp)
            .setContentTitle(this.getString(R.string.notification_title_active))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(this.getString(R.string.notification_big_text_active))
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)

        val n = mBuilder.build()
        startForeground(NOTIFICATION_ID, n)

    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        init()
        return Service.START_STICKY
    }

    private lateinit var sp: SharedPrefs

    private fun init() {
        Log.d(TAG, "yoo-hoo service refreshed")
        if (!started) {
            initSpeechClient()
            initNotification()
            startAudioListener()
            sp = SharedPrefs.getInstance()
            started = true
//            restartOngoing = false
        }
    }


    private fun initSpeechClient() {
        resources.openRawResource(R.raw.credential).use {
            mSpeechClient = SpeechClient.create(
                SpeechSettings.newBuilder()
                    .setCredentialsProvider { GoogleCredentials.fromStream(it) }
                    .build())
        }

    }

    private fun startAudioListener() {
        startStream()
        startAudio()
    }

    private fun startAudio() {
        if (mAudioEmitter == null)
            mAudioEmitter = AudioEmitter()

        mAudioEmitter!!.start { bytes ->
            val builder = StreamingRecognizeRequest.newBuilder()
                .setAudioContent(bytes)

            // if first time, include the config
            if (isFirstRequest.getAndSet(false)) {
                builder.streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(
                        RecognitionConfig.newBuilder()
                            .setLanguageCode("en-US")
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(16000)
                            .build()
                    )
                    .setInterimResults(false)
                    .setSingleUtterance(false)
                    .build()
            }

            // send the next request
            requestStream?.onNext(builder.build())

        }


    }


    private fun startStream() {
        Log.d(TAG, "count: ${++refreshCount}")
        requestStream = mSpeechClient!!.streamingRecognizeCallable()

            .bidiStreamingCall(object : ApiStreamObserver<StreamingRecognizeResponse> {

                override fun onNext(value: StreamingRecognizeResponse) {
                    when {

                        value.resultsCount > 0 -> {

                            val result = value.getResults(0).getAlternatives(0).transcript.toLowerCase().trim()
                            Log.d(TAG, "yoo-hoo: $result")
                            latestHeardPhrase = result
                            Log.d(TAG, "latestHeardPhrase service: $latestHeardPhrase")
                            if (sp.activateWordsSet.contains(result)) {

                                val currentVol = MediaManager.getVol(this@WebSocketService)
                                if (currentVol != YOO_HOO_ACTIVE_VOL_LEVEL) {
                                    sp.lastVol = currentVol
                                }
                                MediaManager.setVol(YOO_HOO_ACTIVE_VOL_LEVEL, this@WebSocketService)
//                                Toast.makeText(this@WebSocketService,getString(R.string.hey_bamboo),Toast.LENGTH_SHORT).show() //todo fix this error "Can't create handler inside thread that has not called Looper.prepare()"
                            } else if (sp.deactivateWordsSet.contains(result)) {
                                MediaManager.setVol(sp.lastVol, this@WebSocketService)
                            }
                        }
                        else -> {
                            Log.d(TAG, "error 0 letters")
                        }
                    }
                }

                override fun onError(t: Throwable) {
                    restartSelf()
                }

                override fun onCompleted() {
                    Log.d(TAG, "stream closed")
                }
            })
    }

    interface ServiceCallback {
        fun heardNewThings(words: String)

    }
}
