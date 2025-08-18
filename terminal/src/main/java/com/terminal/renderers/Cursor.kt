package com.terminal.renderers

/**
 * Represents the cursor in the terminal.
 * @param x The x-coordinate of the cursor.
 * @param y The y-coordinate of the cursor.
 * @param visible Indicates whether the cursor is visible or not.
 */
data class Cursor(val x : Int, val y : Int, val visible: Boolean)
