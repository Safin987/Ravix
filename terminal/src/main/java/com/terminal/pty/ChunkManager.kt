package com.terminal.pty

import android.util.Log
import com.terminal.renderers.TextLine

object ChunkManager {
    private var chunks = mutableListOf<TextLine>()
    //private val array : Array<CharArray> = Array(5000) { CharArray(180) }

    //TODO: Make them independent
    var MAX_COLUMNS = 55
    var MAX_ROWS = 30

    var drawX = 0
    var drawY = 0

    private var currentColumn = 0
    private var currentRow = 0
    private var unitSize = 0F

    fun setUnitChunkWidth(size: Float) {
        unitSize = size
    }

    fun getUnitChunkWidth() = unitSize

    fun parse(array: CharArray) {
        // loop in the array
        for (i in array) {

            if (i == '\n' || currentColumn == MAX_COLUMNS) {

                currentRow++
                currentColumn = 0

                chunks.add(TextLine(MAX_COLUMNS))
                if (i != '\n') {
                    chunks[currentRow].array[currentColumn] = i
                    currentColumn++
                }

            } else {
                if (chunks.isEmpty()) chunks.add(TextLine(MAX_COLUMNS))

                if (i == '\b') {
                    if (currentColumn > 0) currentColumn--
                    chunks[currentRow].array[currentColumn] = ' '
                    continue
                } else if (i == '\r') {
                    currentColumn = 0
                    Log.d(
                        "ChunkManager",
                        "Carriage return encountered. Resetting currentColumn to 0."
                    )
                    continue
                }

                chunks[currentRow].array[currentColumn] = i
                currentColumn++
            }
        }

        //TODO : make a reset function that clears and resets everything
        /*fun reset() {
            chunks.clear()
        }*/
    }

    fun getCurrentChunk(): MutableList<TextLine> = chunks
}