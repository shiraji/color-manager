package com.github.shiraji.colormanager.data

import com.intellij.psi.xml.XmlTag

data class ColorManagerColorTag(val tag: XmlTag,
                                val fileName: String,
                                val isInProject: Boolean,
                                val isInAndroidSdk: Boolean)