package com.terminal.key


interface KeyListeners {
    fun onKeyDown()
    fun onKeyUp()
    fun onKeyRight()
    fun onKeyLeft()

    /**
     *  @param b is the character array representing the key pressed.
     *  if the user presses the ESC key then "\u001b".toCharArray() will be passed.
     *  Note that the key CTRL and ALT are not passed as they are handled separately.
     */
    fun onButtonPressed(b : CharArray)

    /**
     * Sends if the key pressed is a control key.
     */
    fun isCtrl(ctrl : Boolean)

    /**
     * Sends if the key pressed is an alt key.
     */
    fun isAlt(alt : Boolean)


    /**
     * Checks if the control key is still active in the `TerminalView` class.
     * @return `true` if the control key is still active, `false` otherwise.
     *
     * @see com.terminal.view.TerminalView
     */
    fun checkCtrl(): Boolean

    /**
     * Checks if the alternative key is still active in the `TerminalView` class.
     * @return `true` if the alternative key is still active, `false` otherwise.
     *
     * @see com.terminal.view.TerminalView
     */
    fun checkAlt(): Boolean

}