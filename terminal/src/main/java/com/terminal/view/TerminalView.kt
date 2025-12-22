package com.terminal.view

import android.content.Context
import android.graphics.Canvas
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.terminal.key.ControlAlt
import com.terminal.key.KeyListeners
import com.terminal.pty.ChunkManager
import com.terminal.pty.InputListener
import com.terminal.pty.PseudoListener
import com.terminal.renderers.Renderer

/**
 * TerminalView is a custom view that represents a terminal interface. it is the frontend for the terminal emulator.
 *
 * View rendering is handled by the `Renderer` class, and PTY communication is managed through the `PseudoTerminal` class.
 *
 * It handles drawing the terminal content, managing key events, and updating the view with new text chunks using `Renderer`.
 * `TerminalView` must be attached with a `InputListener` to send input events to `PseudoTerminal`.
 *
 * @param context The context in which the view is running.
 *
 * @see Renderer
 * @see com.terminal.pty.PseudoTerminal
 * @see com.terminal.pty.InputListener
 * @see KeyListeners
 * @see PseudoListener
 */
class TerminalView(context: Context) : View(context), KeyListeners, PseudoListener {

    private var isSizeChanged = false
    private var renderer = Renderer(context)
    private lateinit var inputListener: InputListener

    /**
     * Initializes the TerminalView with focusable properties.
     * This allows the view to receive key events and handle input properly.
     */
    init {
        isFocusable = true
        isFocusableInTouchMode = true

        setOnClickListener {
            // Request focus and show keyboard when clicked
            if (requestFocus()) {
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    /**
     * Sets the input listener for this terminal view.
     * This listener will be notified when input is received from the `PseudoTerminal`.
     *
     * @param listener The `InputListener` to be attached to this terminal view.
     * @see PseudoListener
     * @see com.terminal.pty.PseudoTerminal
     */
    fun attachInputListener(listener: InputListener) {
        inputListener = listener
    }


    /**
     * Executes a command in the terminal by sending the command to the `PseudoTerminal` through input listener.
     *
     * @param command The command to be executed, represented as a CharArray.
     * @param enter If true, the command will be followed by a newline character (simulating the Enter key press).
     *
     * @see InputListener.onInput
     * @see com.terminal.pty.PseudoTerminal
     */
    fun executeCommand(command: CharArray, enter: Boolean = true) {
        inputListener.onInput(command)
        if (enter) inputListener.onNewLine()
    }

    /**
     * Canvas is passed to the `Renderer` to draw the terminal content.
     * @see Renderer
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderer.draw(canvas)
    }

    fun setTerminalFont(typeface: android.graphics.Typeface) {
        val charWidth = renderer.getPainter().measureText(" ")
        renderer.updateTerminalFont(typeface)
        ChunkManager.setUnitChunkWidth(charWidth)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!isSizeChanged) {
            val charWidth = renderer.getPainter().measureText(" ") // Monospace
            ChunkManager.MAX_ROWS = (w / charWidth).toInt()
            ChunkManager.MAX_COLUMNS=(h / renderer.getLineHeight())
            //inputListener.onResize((w / charWidth).toInt(), h / renderer.getLineHeight())
            isSizeChanged = true
        }
    }


    /**
     * triggered when the 'PseudoTerminal' is updated with new chunks of text.
     */
    override fun onUpdate(start : Int , end : Int) {
        // renderer.setChunks(chunks)
        invalidate() // Redraw the view with the updated chunks
    }

    override fun onCheckIsTextEditor(): Boolean = true

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        //outAttrs.inputType = InputType.TYPE_CLASS_TEXT
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

        return object : BaseInputConnection(this, true) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                if (!text.isNullOrEmpty()) {
                    inputListener.onInput(text.toString().toCharArray())
                }
                return true
            }

        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                inputListener.onNewLine()
                true
            }

            KeyEvent.KEYCODE_DEL -> {
                inputListener.onInput(charArrayOf(0x7F.toChar())) // ASCII DEL
                true
            }

            else -> {
                val char = event.unicodeChar.toChar()
                val sendChar = if (ControlAlt.isCtrl.value!! && char != 0.toChar()) {
                    Log.d("Input", "Key pressed: $char (Ctrl: ${ControlAlt.isCtrl.value})")

                    ControlAlt.isCtrl.value = false
                    // Map Ctrl + key to control code
                    when (char) {
                        in 'a'..'z' -> (char.uppercaseChar().code - 'A'.code + 1).toChar()
                        in 'A'..'Z' -> (char.code - 'A'.code + 1).toChar()
                        else -> char
                    }
                } else if (ControlAlt.isAlt.value!! && char != 0.toChar()) {
                    Log.d("Input", "Key pressed: $char (Alt: ${ControlAlt.isAlt.value})")

                    ControlAlt.isAlt.value = false
                    // Alt is usually sent as ESC + char
                    val esc = 0x1B.toChar()
                    inputListener.onInput(charArrayOf(esc, char))
                    0.toChar() // Already sent both characters
                } else char

                if (sendChar != 0.toChar()) {
                    inputListener.onInput(charArrayOf(sendChar))
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
        }
    }

    /**
     * Returns the soft key listener associated with this terminal view.
     * @see com.terminal.key.SoftKeyView
     */
    fun getKeyListener(): KeyListeners {
        return this
    }

    override fun onKeyDown() {}
    override fun onKeyUp() {}
    override fun onKeyRight() {}
    override fun onKeyLeft() {}

    /**
     *  @param b is the character array representing the key pressed.
     *  if the user presses the ESC key then "\u001b".toCharArray() will be passed.
     *  Note that the key CTRL and ALT are not passed as they are handled separately.
     */
    override fun onButtonPressed(b: CharArray) {
        inputListener.onInput(b)
    }

}