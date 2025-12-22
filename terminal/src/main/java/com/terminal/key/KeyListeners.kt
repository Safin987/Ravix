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


}