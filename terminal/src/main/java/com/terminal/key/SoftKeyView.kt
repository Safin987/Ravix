package com.terminal.key

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import com.terminal.R

class SoftKeyView(c: Context) : LinearLayout(c) {
    private var keyListener: KeyListeners? = null

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

    fun bindLifecycle(owner: LifecycleOwner) {
        ControlAlt.isCtrl.observe(owner) { active ->
            if (active) {
                findViewById<Button>(R.id.ctrl).setTextColor(Color.CYAN)
            } else {
                findViewById<Button>(R.id.ctrl).setTextColor(Color.WHITE)
            }
        }
        ControlAlt.isAlt.observe(owner) { active ->
            if (active) {
                findViewById<Button>(R.id.alt).setTextColor(Color.CYAN)
            } else {
                findViewById<Button>(R.id.alt).setTextColor(Color.WHITE)
            }
        }
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
                if (id == R.id.ctrl) {
                    ControlAlt.isCtrl.value = !(ControlAlt.isCtrl.value ?: false)
                } else if (id == R.id.alt) {
                    ControlAlt.isAlt.value = !(ControlAlt.isAlt.value ?: false)
                }
            }
        }
    }

    fun handleSoftKey(key: String) {

        val charArray = when (key) {

            "TAB" -> {
                "\t".toCharArray() // Tab character
            }

            "ESC" -> {
                "\u001b".toCharArray() // Escape character
            }

            "CTRL" -> {
                null // Handled separately
            }

            "ALT" -> {
                null // Handled separately
            }

            else -> {
                "".toCharArray()
            }
        }
        charArray?.let {
            keyListener?.onButtonPressed(it)
        }


    }
}