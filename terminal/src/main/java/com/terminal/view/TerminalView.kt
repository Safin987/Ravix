package com.terminal.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.terminal.key.KeyListeners
import com.terminal.pty.InputListener
import com.terminal.pty.PseudoListener
import com.terminal.renderers.Renderer
import com.terminal.renderers.TextLine

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

    private var ctrlActive = false
    private var altActive = false
    private var isSizeChanged = false
    private var renderer = Renderer(this, context)
    private lateinit var inputListener: InputListener

    /**
     * Initializes the TerminalView with focusable properties.
     * This allows the view to receive key events and handle input properly.
     */
    init {
        isFocusable = true
        isFocusableInTouchMode = true
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
     * @see com.terminal.pty.InputListener.onInput
     * @see com.terminal.pty.PseudoTerminal
     */
    fun executeCommand(command: CharArray, enter: Boolean = true) {
        inputListener.onInput(command)
        if (enter) inputListener.onNewLine()
    }

    /**
     * Sets the font of the terminal. Redraw is called after updating the font.
     * It is recommended to use monospace fonts for proper alignment and readability.
     */
    fun setTerminalFont(typeface: Typeface) {
        renderer.setFont(typeface)
    }

    /**
     * Sets the text size of the terminal. Redraw is called after updating the text size.
     */
    fun setTextSize(size: Float) {
        renderer.setTextSize(size)
    }

    /**
     * Canvas is passed to the `Renderer` to draw the terminal content.
     * @see Renderer
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderer.draw(canvas)
    }

    // The number of rows and columns is calculated based on the view size.Note that this is done only once when the size changes for first time.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!isSizeChanged) {
            val charWidth = renderer.getPainter().measureText(" ") // Monospace
            renderer.setMaxColumns((w / charWidth).toInt())
            renderer.setMaxRow(h / renderer.getLineHeight())
            isSizeChanged = true
        }
    }

    /**
     * triggered when the 'PseudoTerminal' is updated with new chunks of text.
     * @param chunks A mutable list of `TextLine` objects representing the updated text.
     * 'Renderer` will save it as a reference and will be used to render the terminal view.
     * @see com.terminal.pty.PseudoTerminal.onOutputFromNative
     * @see com.terminal.view.TerminalView
     * @see Renderer
     */
    override fun onUpdate(chunks: MutableList<TextLine>) {
        renderer.setChunks(chunks)
        invalidate() // Redraw the view with the updated chunks
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                inputListener.onNewLine()
                true
            }
            else -> {
                val char = event.unicodeChar.toChar()
                if (char != 0.toChar()) {
                    inputListener.onInput(charArrayOf(char))
                    Log.d("Input", "Key pressed: $char")
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

    override fun onKeyDown() {

    }

    override fun onKeyUp() {

    }

    override fun onKeyRight() {
    }

    override fun onKeyLeft() {

    }

    /**
     *  @param b is the character array representing the key pressed.
     *  if the user presses the ESC key then "\u001b".toCharArray() will be passed.
     *  Note that the key CTRL and ALT are not passed as they are handled separately.
     */
    override fun onButtonPressed(b: CharArray) {
        inputListener.onInput(b)
    }

    override fun isCtrl(ctrl: Boolean) {
        ctrlActive = ctrl
    }

    override fun isAlt(alt: Boolean) {
        altActive = alt
    }

    /**
     * Checks if the control key is still active in the `TerminalView` class.
     * @return `true` if the control key is still active, `false` otherwise.
     *
     * @see com.terminal.view.TerminalView
     */
    override fun checkCtrl(): Boolean = ctrlActive

    /**
     * Checks if the alternative key is still active in the `TerminalView` class.
     * @return `true` if the alternative key is still active, `false` otherwise.
     *
     * @see com.terminal.view.TerminalView
     */
    override fun checkAlt(): Boolean = altActive

    /**
     * Returns the chunk update listener associated with this terminal view.
     * This is used to receive updates from the `PseudoTerminal` when new chunks of text are available.
     * @see com.terminal.pty.PseudoTerminal
     */
    fun getUpdateListener(): PseudoListener {
        return this
    }

}