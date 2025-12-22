package com.terminal.pty

/**
 * Singleton object to hold the current window size of the terminal.
 * It contains two properties: `row` and `col`, which represent the number of rows and columns in the terminal window.
 */
object WindowSize {
    var row : Int = 0
    var col : Int = 0
}