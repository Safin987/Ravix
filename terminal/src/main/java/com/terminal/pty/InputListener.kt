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
}