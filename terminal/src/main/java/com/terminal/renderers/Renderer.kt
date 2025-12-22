package com.terminal.renderers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import com.terminal.pty.ChunkManager
import com.terminal.pty.ChunkManager.getCurrentChunk
import com.terminal.view.TerminalView

/**
 * Renderer class responsible for drawing the terminal content on the provided canvas.
 * It handles font, text size, and line height calculations.
 *
 * @param context The context used to access resources like fonts.
 *
 * @see TerminalView
 */
class Renderer(context: Context) {

    private var monoFont =
        Typeface.createFromAsset(context.assets, "fonts/JetBrainsMono-Medium.ttf")
    private val painter = Paint().apply {
        setColor(Color.WHITE)
        textSize = 40f
        isAntiAlias = true
        setTypeface(monoFont)
    }
    fun getPainter(): Paint = painter

    private var lineHeight: Int = measureLineHeight()
    fun measureLineHeight() : Int = (painter.fontMetrics.descent - painter.fontMetrics.ascent).toInt()
    fun getLineHeight(): Int = lineHeight

    fun updateTerminalFont(typeface: Typeface) {
        monoFont = typeface
        painter.typeface = monoFont
        lineHeight = measureLineHeight()
    }


    /**
     * Index of the top chunk of the screen. starts at 0.
     */
    private var chunkIndex = 0

    /**
     * Draws the terminal content on the provided canvas.
     * This method is called from the `TerminalView`'s `onDraw` method
     * This method is not fully implemented yet.
     */
    //TODO("Implement proper chunk handling")
    fun draw(c: Canvas) {
        var y = lineHeight.toFloat()
        var x = 20F
        for (i in chunkIndex..(chunkIndex + ChunkManager.MAX_ROWS).coerceAtMost(getCurrentChunk().size - 1)) {
            c.drawText(getCurrentChunk()[i].array,0,getCurrentChunk()[i].array.size, x,y,painter)
            x += getCurrentChunk()[i].array.size * ChunkManager.getUnitChunkWidth()
            y += lineHeight
            //Log.d("Renderer", "Drawing chunk $i at position ($x, $y)")
        }
    }
}