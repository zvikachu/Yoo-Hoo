package com.amplez.yoo_hoo

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView

class sd {


    private var et: TextView? = null

    fun asd() {
        et = null
        et!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val strEnteredVal = et!!.text.toString()

                if (strEnteredVal != "") {
                    val num = Integer.parseInt(strEnteredVal)
                    if (num < 60) {
                        et!!.text = "" + num
                    } else {
                        et!!.text = ""
                    }
                }
            }
        })
    }
}

