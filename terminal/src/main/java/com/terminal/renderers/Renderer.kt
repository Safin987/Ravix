package com.terminal.renderers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.terminal.view.TerminalView

/**
 * Renderer class responsible for drawing the terminal content on the provided canvas.
 * It handles font, text size, and line height calculations.
 *
 * @param view The `TerminalView` instance where the content will be drawn.
 * @param context The context used to access resources like fonts.
 *
 * @see TerminalView
 */
class Renderer(private val view: TerminalView, context: Context) {

    private var chunks = mutableListOf<TextLine>()
    private var monoFont =
        Typeface.createFromAsset(context.assets, "fonts/JetBrainsMono-Medium.ttf")

    private var maxRows: Int = 0
    private var maxColumns: Int = 0
   // private val cursor = Cursor(0f, 0f, true)

    private val painter = Paint().apply {
        setColor(Color.WHITE)
        textSize = 40f
        isAntiAlias = true
        setTypeface(monoFont)
    }

    private val fontMetrics: Paint.FontMetrics = painter.fontMetrics

    private val lineHeight: Int = (fontMetrics.descent - fontMetrics.ascent).toInt()

    fun getLineHeight(): Int = lineHeight
    fun getPainter(): Paint = painter

    /**
     * Sets the font of the terminal.
     */
    fun setFont(typeface: Typeface) {
        monoFont = typeface
        view.invalidate()
    }

    /**
     * Sets the text size of the terminal.
     */
    fun setTextSize(size: Float) {
        painter.textSize = size
        view.invalidate()
    }

    /**
     * Sets the maximum number of rows that can be displayed in the terminal.
     */
    fun setMaxRow(r: Int) {
        maxRows = r
    }

    /**
     * Sets the maximum number of columns that can be displayed in the terminal.
     */
    fun setMaxColumns(c: Int) {
        maxColumns = c
    }

    /**
     * Sets the chunks of text to be rendered.
     * This method is called from the `TerminalView` when new chunks are received from the `PseudoTerminal`.
     */
    fun setChunks(newCHunks: MutableList<TextLine>) {
        chunks = newCHunks
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
        for ( i in chunkIndex..(chunkIndex + maxRows).coerceAtMost(chunks.size - 1)) {
            var x = 50F
            for (segment in chunks[chunkIndex].getSegments()) {
                c.drawText(segment.chars, 0, segment.chars.size, x, y, painter)
                x+=painter.measureText(segment.chars,0,segment.chars.size)
            }
            y+=lineHeight
        }
    }

}