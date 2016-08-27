package com.github.shiraji.colormanager.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class ColorManagerToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val panel = ColorManagerToolWindowPanel(project)
        val content = contentManager.factory.createContent(panel, null, false)
        contentManager.addContent(content)
        Disposer.register(project, panel)
    }
}