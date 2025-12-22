package com.terminal.pty

interface PseudoListener {
    /**
     * triggers `TerminalView` when the `PseudoTerminal` is updated with new chunks of text.
     * @param chunks A mutable list of `TextLine` objects representing the updated text.
     * @param start starting point of the row that to be redrawn
     * `TerminalView` will send it to `Renderer` to keep as a reference to the Chunks and will be used to render the view when `TerminalView` is redrawn.
     *
     * @see com.terminal.pty.PseudoTerminal.onOutputFromNative
     * @see com.terminal.view.TerminalView.onUpdate
     * @see com.terminal.renderers.Renderer.draw
     */
    fun onUpdate(
        start: Int, end: Int
    )

}