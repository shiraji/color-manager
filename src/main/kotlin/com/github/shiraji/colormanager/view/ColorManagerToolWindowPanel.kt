package com.github.shiraji.colormanager.view

import com.github.shiraji.colormanager.data.ColorManagerColorTag
import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.xml.XmlFile
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

class ColorManagerToolWindowPanel(val project: Project) : SimpleToolWindowPanel(true, true), DataProvider, Disposable {

    val listModel: DefaultListModel<String> = DefaultListModel()

    val colorMap: MutableMap<String, ColorManagerColorTag> = mutableMapOf()

    val FILETER_XML = listOf("AndroidManifest.xml", "strings.xml", "dimens.xml", "base_strings.xml", "pom.xml", "donottranslate-cldr.xml", "donottranslate-maps.xml", "common_strings.xml")

    var filterLibRes = true

    init {
        setToolbar(createToolbarPanel())
        setContent(createContentPanel())
    }

    private fun createContentPanel(): JComponent {
        initColorMap()
        colorMap.forEach {
            listModel.addElement(it.key)
        }
        val list = JBList(listModel)
        list.cellRenderer = object : DefaultListCellRenderer() {
            val colorTextList = mutableListOf<String>()

            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                val colorName = list?.model?.getElementAt(index)
                if (colorName is String) {
                    colorTextList.clear()
                    setBackgroundColorFromKey(colorName)
                }
                foreground = Color.BLACK
                return component
            }

            private fun setBackgroundColorFromKey(colorName: String) {
                colorMap[colorName]?.let {
                    colorTag ->
                    val colorText = colorTag.tag.value.trimmedText
                    if (colorTextList.contains(colorText)) {
                        background = Color.WHITE
                        return
                    }
                    colorTextList.add(colorText)
                    if (colorText.startsWith("@android:color/")) {
                        setBackgroundColorFromKey("R.color.${colorText.replace("@android:color/", "")}")
                    } else if (colorText.startsWith("@color/")) {
                        setBackgroundColorFromKey("R.color.${colorText.replace("@color/", "")}")
                    } else if (colorText.startsWith("#")) {
                        background = when (colorText.length) {
                            4 -> {
                                val newColorText = "#${colorText[1]}${colorText[1]}${colorText[2]}${colorText[2]}${colorText[3]}${colorText[3]}"
                                Color.decode(newColorText)
                            }
                            7 -> Color.decode(colorText)
                            9 -> Color(java.lang.Long.decode(colorText).toInt(), true)
                            else -> background
                        }
                    } else {
                        background = Color.WHITE
                    }
                }
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

                val gotoMenu = JMenuItem("Go to $selectedColor").apply {
                    addActionListener {
                        colorMap[selectedColor]?.let {
                            (it.tag as? XmlTagImpl)?.navigate(true)
                        }
                    }
                }

                JPopupMenu().run {
                    add(copyMenu)
                    add(gotoMenu)
                    show(e.component, e.x, e.y)
                }
            }
        })

        return ScrollPaneFactory.createScrollPane(list)
    }

    private fun createToolbarPanel(): JComponent? {
        val group = DefaultActionGroup()
        group.add(RefreshAction())
        group.add(FileterAction())
        val actionToolBar = ActionManager.getInstance().createActionToolbar("ColorManager", group, true)
        return JBUI.Panels.simplePanel(actionToolBar.component)
    }

    override fun dispose() {
    }

    private fun initColorMap() {
        val psiManager = PsiManager.getInstance(project)

        FileTypeIndex.getFiles(XmlFileType.INSTANCE, ProjectScope.getProjectScope(project)).forEach {
            addToColorMap(psiManager, it, true)
        }

        if (!filterLibRes) {
            FileTypeIndex.getFiles(XmlFileType.INSTANCE, ProjectScope.getLibrariesScope(project)).forEach {
                addToColorMap(psiManager, it, false)
            }
        }
    }

    private fun addToColorMap(psiManager: PsiManager, virtualFile: VirtualFile, isInProject: Boolean) {
        if (FILETER_XML.contains(virtualFile.name)) return
        val xmlFile = psiManager.findFile(virtualFile) as? XmlFile ?: return
        xmlFile.rootTag?.findSubTags("color")?.forEach {
            colorMap.put("R.color.${it.getAttribute("name")?.value}", ColorManagerColorTag(it, isInProject))
        }
    }

    private fun refreshListModel() {
        listModel.removeAllElements()
        colorMap.clear()

        initColorMap()
        colorMap.forEach {
            listModel.addElement(it.key)
        }
    }

    inner class RefreshAction() : AnAction("Reload colors", "Reload colors", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent?) {
            refreshListModel()
        }
    }

    inner class FileterAction() : ToggleAction("Filter library resource colors", "Filter library resource colors", AllIcons.General.Filter) {

        override fun isSelected(e: AnActionEvent?) = filterLibRes

        override fun setSelected(e: AnActionEvent?, state: Boolean) {
            filterLibRes = state
            refreshListModel()
        }
    }
}