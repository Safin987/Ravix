package com.terminal.key

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.terminal.R

class SoftKeyView(c: Context) : LinearLayout(c) {
    private var keyListener: KeyListeners? = null

    private var ctrl = false
    private var alt = false

    /**
     * Attaches a key listener to the soft keyboard.
     * This listener will be notified when a key is pressed.
     */
    fun attachKeyListener(listener: KeyListeners) {
        keyListener = listener
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.softkey, this, true)
        setUpOnClick()
    }

    /**
     *  sets on click listener for all the keys in the soft keyboard.
     */
    fun setUpOnClick() {
        val buttons = mapOf(
            R.id.ctrl to "CTRL",
            R.id.alt to "ALT",
            R.id.tab to "TAB",
            R.id.esc to "ESC"
        )

        buttons.forEach { (id, label) ->
            rootView.findViewById<Button>(id).setOnClickListener {
                handleSoftKey(label)
            }
        }
    }

    fun handleSoftKey(key: String) {
        var charArray = "".toCharArray()

        when (key) {

            "CTRL" -> {
                ctrl = !keyListener!!.checkCtrl()
                keyListener?.isCtrl(ctrl)
            }

            "ALT" -> {
                alt = !keyListener!!.checkAlt()
                keyListener?.isAlt(alt)
            }

            "TAB" -> {
                charArray = "\t".toCharArray() // Tab character
            }

            "ESC" -> {
                charArray = "\u001b".toCharArray() // Escape character
            }

            else -> throw RuntimeException("Invalid key was pressed")
        }

        keyListener?.onButtonPressed(
            charArray
        )
    }
}