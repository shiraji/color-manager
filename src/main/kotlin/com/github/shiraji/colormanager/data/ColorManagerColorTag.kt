package com.github.shiraji.colormanager.data

import com.intellij.psi.xml.XmlTag
import java.awt.Color

data class ColorManagerColorTag(val tag: XmlTag,
                                var color: Color? = null,
                                var colorCode: String? = null,
                                var errorMessage: String? = null,
                                val fileName: String,
                                val isInProject: Boolean,
                                val isInAndroidSdk: Boolean)