package com.terminal.pty

import android.util.Log
import com.terminal.renderers.LineSegment
import com.terminal.renderers.TextLine
import com.terminal.view.TerminalView

/**
 * This class is used to interact with the native terminal.
 * It provides methods to start the pseudo terminal, send input, and update the window size.
 * The native methods are implemented in C/C++ and are loaded using JNI.
 * @param listener A listener that will be notified of chunk updates.
 */
class PseudoTerminal(listener: PseudoListener) : InputListener {

    private var chunks = mutableListOf<TextLine>()

    private val pseudoListener: PseudoListener = listener

    init {
        System.loadLibrary("terminal")
    }
    external fun startPty()
    external fun send(inp: CharArray, interactive : Boolean)
    external fun updateWinSize(r: Int, c: Int)
    external fun simulateNewLine()

    private lateinit var terminalView: TerminalView

    /**
     * This method is called from the native code to send output to the terminal.
     */
    //This method is not properly implemented yet.
    //TODO: Send the received bytes to a parser to handle ANSI escape codes, then update the terminal view according to the received output
    fun onOutputFromNative(bytes: ByteArray) {
        val text = String(bytes, Charsets.UTF_8)
        Log.d("PseudoTerminal", "Received output: $text")
        mergeNewLines(text)
        pseudoListener.onUpdate(chunks)
    }

    /**
     * attach the `TerminalView` to the `PseudoTerminal`
     */
    fun attachView(view: TerminalView) {
        terminalView = view
    }

    /**
     * Starts the pseudo terminal.
     */
    fun start() = startPty()

    /**
     * Sends input to the pseudo terminal.
     * @param input The input CharArray to be sent to the terminal.
     */
    fun sendInput(input: CharArray) {
        Log.d("PseudoTerminal", "Sending input: $input")
        send(input,true)
    }

    /**
     * Updates the window size of the pseudo terminal. Redraw is called after updating the size.
     * @param rows The number of rows in the terminal.
     * @param columns The number of columns in the terminal.
     */
    fun updateWindowSize(rows: Int, columns: Int) {
        Log.d("PseudoTerminal", "Updating window size to $rows rows and $columns columns")
        updateWinSize(rows, columns)
        terminalView.invalidate()
    }


    //This method is not properly implemented yet.
    //TODO: merge the new lines into the existing chunks properly complying with the terminal's line and ansi structure.
    private fun mergeNewLines(text: String) {
        /*val lines = text.split("\n")
        if (chunks.isNotEmpty() && lines.isNotEmpty()) {
            val updatedChunk = chunks.last() + lines.first()
            chunks[chunks.lastIndex] = updatedChunk
            chunks.addAll(lines.drop(1))
        }else{
            chunks.addAll(lines)
        }*/
        chunks.add(TextLine(mutableListOf(LineSegment(text.toCharArray(), 0, ByteArray(0)))))
        Log.d("mergeNewLines ","chunks : $chunks")
    }

    /**
     * Called when input is received from the `TerminalView`
     * @param input The input string received.
     *
     * @see sendInput
     */
    override fun onInput(input: CharArray) {
        Log.d("PseudoTerminal", "Input received: $input")
        sendInput(input)
    }

    override fun onNewLine() = simulateNewLine()
}