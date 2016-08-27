package com.github.shiraji.colormanager.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.TextTransferable
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ColorManagerToolWindowPanel(val project: Project) : SimpleToolWindowPanel(false, true), DataProvider, Disposable {

    val listModel: DefaultListModel<String> by lazy {
        DefaultListModel<String>()
    }

    val colorMap: MutableMap<String, XmlTag> = mutableMapOf()

    init {
        setContent(createContentPanel())
        setToolbar(createToolbarPanel())
    }

    private fun createContentPanel(): JComponent {
        initColorMap()
        colorMap.forEach {
            listModel.addElement(it.key)
        }
        val list = JBList(listModel)
        list.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                colorMap[list?.model?.getElementAt(index)]?.let {
                    xmlTag ->
                    val colorText = xmlTag.value.trimmedText
                    if (colorText.length == 7) {
                        background = Color.decode(colorText)
                    } else if (colorText.length == 9) {
                        background = Color(java.lang.Long.decode(colorText).toInt(), true)
                    }
                }
                foreground = Color.BLACK
                return component
            }
        }
        // Google recommended height!!!
        list.fixedCellHeight = 48
        ListSpeedSearch(list)

        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                super.mouseClicked(e)
                if (!SwingUtilities.isRightMouseButton(e)) return
                if (list.isEmpty) return
                e ?: return

                val selectedColor = listModel.get(list.minSelectionIndex)
                val copyMenu = JMenuItem("Copy $selectedColor").apply {
                    addActionListener {
                        CopyPasteManager.getInstance().setContents(TextTransferable(selectedColor))
                    }
                }

                JPopupMenu().run {
                    add(copyMenu)
                    show(e.component, e.x, e.y)
                }
            }
        })

        return ScrollPaneFactory.createScrollPane(list)
    }

    private fun createToolbarPanel(): JPanel {
        val group = DefaultActionGroup()
        group.add(RefreshAction())
        val actionToolBar = ActionManager.getInstance().createActionToolbar("ColorFinder", group, false)
        return JBUI.Panels.simplePanel(actionToolBar.component)
    }

    override fun dispose() {
    }

    private fun initColorMap() {
        FilenameIndex.getFilesByName(project, "colors.xml", GlobalSearchScope.projectScope(project)).forEach {
            colorFile ->
            (colorFile as? XmlFile)?.rootTag?.findSubTags("color")?.forEach {
                colorMap.put("R.color.${it.getAttribute("name")?.value}", it)
            }
        }
    }

    inner class RefreshAction() : AnAction("Reload colors.xml", "Reload colors.xml", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent?) {
            ApplicationManager.getApplication().runWriteAction {
                initColorMap()
                listModel.removeAllElements()
                colorMap.forEach {
                    listModel.addElement(it.key)
                }
            }
        }
    }
}