package com.github.shiraji.colormanager.data

data class ColorManagerCell(val colorName: String, val colorCode: String) {

    companion object {
        const val SEPARATOR = ":::"
    }

    override fun toString(): String {
        return "$colorName$SEPARATOR$colorCode"
    }
}