package com.terminal.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.terminal.pty.PseudoTerminal
import com.terminal.key.SoftKeyView

class TerminalContainer@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val terminalView = TerminalView(context)
    private val softKeyView : SoftKeyView = SoftKeyView(context).apply {
        attachKeyListener(terminalView.getKeyListener())
    }
    val pseudoTerminal = PseudoTerminal(terminalView.getUpdateListener()).apply {
        terminalView.attachInputListener(this)
        startPty()
        terminalView.executeCommand("stty -a".toCharArray()) // This command checks terminal settings
    }


    init {
        orientation = VERTICAL

        // Add terminalView with weight to fill remaining space
        addView(terminalView, LayoutParams(
            LayoutParams.MATCH_PARENT,
            0, // height 0 because weight will define it
            1f // weight = fills remaining space
        ).apply {
            // Optional: limit max width
            width = LayoutParams.MATCH_PARENT
        })

        // Add softKeyView at bottom with wrap content height
        addView(softKeyView, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ))

    }

}