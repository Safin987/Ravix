package com.terminal.pty

interface InputListener {
    /**
     * Called when input is received from the `TerminalView`
     * @param input The input CharArray received.
     *
     * @see com.terminal.pty.PseudoTerminal.sendInput
     */
    fun onInput(input: CharArray)

    fun onNewLine()

    /**
     * Called when the terminal is resized.
     * @param rows The number of rows in the terminal.
     * @param columns The number of columns in the terminal.
     *
     */
    fun onResize(rows: Int, columns: Int)
}