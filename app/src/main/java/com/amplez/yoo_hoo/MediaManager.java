package com.amplez.yoo_hoo;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class MediaManager {


    private static int volLevel;
    private MediaPlayer alarmMp;


    public void loopAlarm(Context context, int alarmRaw) {
        alarmMp = MediaPlayer.create(context, alarmRaw);
        if (alarmMp.isPlaying()) {
            return;
        }

        alarmMp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                alarmMp.start();
            }
        });
        alarmMp.start();

    }

    public void stopAlarm() {
        alarmMp.stop();

    }

    public static void adjustVol(Context context, boolean raiseVol) {
        AudioManager am =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (raiseVol && volLevel < am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            volLevel++;
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volLevel, 0);
            volLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        } else if (!raiseVol && volLevel > 0){
            volLevel--;
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volLevel, 0);
            volLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        }


    }

    public static void maintainVolLevel(Context context) {
        setVol(0, context);
    }


    public static void setVolMax(Context context) {
        AudioManager am =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        ;
    }

    public static void setVolMin(Context context) {
        AudioManager am =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        ;
    }

    public static void setVol(float percent, Context context) {
        AudioManager am =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (percent != 0)
            volLevel = (int) (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * percent);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volLevel, 0);
    }
    public static void setVol(int exactValue, Context context) {
        AudioManager am =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volLevel = exactValue;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volLevel, 0);
    }
    public static int getVol(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volLevel = (am.getStreamVolume(AudioManager.STREAM_MUSIC));
        return volLevel;
    }

    public static void tick(Context context) {
        MediaPlayer.create(context, R.raw.tick1).start();
    }
}
