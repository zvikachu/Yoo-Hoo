package com.amplez.yoo_hoo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by shabat on 27/07/2017.
 */

public class SharedPrefs {

    private static final String KEY_LAST_VOL = "lastVol";
    private static final String KEY_ACTIVATE_WORDS = "activateWords";
    private static final String KEY_DEACTIVATE_WORDS = "deactivateWords";


    private static SharedPreferences sp;
    private SharedPreferences.Editor editor;


    //variables
    private static SharedPrefs sharedPrefs;
    private boolean spInitiated;
    private Context mContext;


    private SharedPrefs() {
    }


    public static SharedPrefs getInstance() {
        if (sharedPrefs == null) {
            sharedPrefs = new SharedPrefs();
        }
        return sharedPrefs;
    }


    public SharedPrefs initSharedPrefs(Activity activity) {
        if (spInitiated)
            return sharedPrefs;
        setInstances(activity);
        spInitiated = true;
        return sharedPrefs;
    }


    private void setInstances(Context context) {
        mContext = context;
        sp = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        editor = sp.edit();
    }


    public void setLastVol(int vol) {
        editor.putInt(KEY_LAST_VOL, vol).apply();

    }

    public void setActivateWords(String activateWords) {
        Set<String> wordSet = stringToStringSet(activateWords);
        if (wordSet == null)
            wordSet = getDefaultActivateWords();
        editor.putStringSet(KEY_ACTIVATE_WORDS, wordSet).commit();

    }

    public void setDeactivateWords(String deactivateWords) {
        Set<String> wordSet = stringToStringSet(deactivateWords);
        if (wordSet == null)
            wordSet = getDefaultDeactivateWords();
        editor.putStringSet(KEY_DEACTIVATE_WORDS, wordSet).commit();

    }

    private static HashSet<String> stringToStringSet(String wordsStr) {
        if (wordsStr == null)
            return null;
        wordsStr = wordsStr.trim().replaceAll(" +", " ");
        wordsStr = wordsStr.replaceAll(",+", ",");
        wordsStr = wordsStr.replace(" ,", ",");
        wordsStr = wordsStr.replace(", ", ",");
        while (wordsStr.endsWith(",")) {
            wordsStr = wordsStr.substring(0, wordsStr.length() - 1);
        }
        wordsStr = wordsStr.trim();
        wordsStr = wordsStr.toLowerCase();
        if (wordsStr.isEmpty())
            return null;
        String[] words = wordsStr.split(",");
        return new HashSet<>(Arrays.asList(words));

    }

    public int getLastVol() {
        return sp.getInt(KEY_LAST_VOL, 0);

    }

    public Set<String> getActivateWordsSet() {
        Set<String> words = sp.getStringSet(KEY_ACTIVATE_WORDS, getDefaultActivateWords());
        if (words == null || words.size() < 1) {
            return getDefaultActivateWords();
        }
        return words;

    }

    public Set<String> getDeactivateWordsSet() {
        Set<String> words = sp.getStringSet(KEY_DEACTIVATE_WORDS, getDefaultDeactivateWords());
        if (words == null || words.size() < 1) {
            return getDefaultDeactivateWords();
        }
        return words;
    }

    private Set<String> getDefaultActivateWords() {
        return stringToStringSet(mContext.getString(R.string.comma_separated_activate_phrases));
    }

    private Set<String> getDefaultDeactivateWords() {
        return stringToStringSet(mContext.getString(R.string.comma_separated_deactivate_phrases));
    }


}
