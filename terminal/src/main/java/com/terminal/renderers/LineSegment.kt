package com.terminal.renderers

/**
 * Represents a segment of a line in the terminal. Each segment consists of characters, a color, and styles.
 * @param chars The characters in the segment.
 * @param color The color of the segment, represented as an integer.
 * @param styles The styles applied to the segment, represented as a byte array.
 */
data class LineSegment(val chars : CharArray, val color : Int, val styles : ByteArray)