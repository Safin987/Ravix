package com.terminal.pty

import android.util.Log
import com.terminal.view.TerminalView
import java.nio.ByteBuffer

/**
 * This class is used to interact with the native terminal.
 * It provides methods to start the pseudo terminal, send input, and update the window size.
 * The native methods are implemented in C/C++ and are loaded using JNI.
 */
class PseudoTerminal() : InputListener {

    init {
        System.loadLibrary("terminal")
    }
    external fun startPty()
    external fun send(inp: CharArray, interactive : Boolean)
    external fun updateWinSize(r: Int, c: Int)
    external fun simulateNewLine()

    private var terminalView: TerminalView? = null

    /**
     * This method is called from the native code to send output to the terminal.
     */
    fun onOutputFromNative(bytes: ByteArray) {
        val decoder = Charsets.UTF_8.newDecoder()
        val charBuffer = decoder.decode(ByteBuffer.wrap(bytes))
        val charArray = CharArray(charBuffer.remaining())
        charBuffer.get(charArray)
        Log.d("PseudoTerminal", "Received output: ${charArray.joinToString("")}")
        ChunkManager.parse(charArray)
        terminalView?.let {
            Log.d("PseudoTerminal", "Invalidating terminal view")
            it.invalidate()
        }
    }
    fun attachView(tv: TerminalView) {
        terminalView = tv
    }

    /**
     * Sends input to the pseudo terminal.
     * @param input The input CharArray to be sent to the terminal.
     */
    fun sendInput(input: CharArray) {
        Log.d("PseudoTerminal", "Sending input: $input")
        send(input, true)
    }

    /**
     * Called when input is received from the `TerminalView`
     * @param input The input string received.
     *
     * @see sendInput
     */
    override fun onInput(input: CharArray) = sendInput(input)

    override fun onNewLine() = simulateNewLine()

    /**
     * Updates the window size of the pseudo terminal. Redraw is called after updating the size.
     * @param rows The number of rows in the terminal.
     * @param columns The number of columns in the terminal.
     */
    override fun onResize(rows: Int, columns: Int) {
        Log.d("PseudoTerminal", "Updating window size to $rows rows and $columns columns")
        updateWinSize(rows, columns)
        terminalView?.invalidate()
    }
}
