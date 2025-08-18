package com.terminal.renderers

class TextLine(segments: MutableList<LineSegment>)  {
    private val lineSegment = segments

    /**
     * Returns the list of line segments that make up this text line.
     */
    fun getSegments(): MutableList<LineSegment> {
        return lineSegment
    }
}