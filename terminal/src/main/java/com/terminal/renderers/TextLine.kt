package com.terminal.renderers

import android.graphics.Color

class TextLine(val colSize: Int) {
    fun getWidth(): Int = colSize
    var array = CharArray(colSize) { i -> ' ' }
    private val styles = IntArray(colSize) { i -> Color.WHITE }
}