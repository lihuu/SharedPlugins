package com.github.lihuu.sharedplugins.ui

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

/**
 * A dialog window that displays a list of plugins for the user to select.
 * It uses a JBTable to show plugin names and versions, with a checkbox for selection.
 */
class SelectPluginsDialog(private val plugins: List<IdeaPluginDescriptor>) : DialogWrapper(true) {

    private lateinit var table: JBTable
    private val columnNames = arrayOf("Copy", "Plugin Name", "Version")
    private val dataModel = object : DefaultTableModel(columnNames, 0) {
        // Make the first column (checkbox) editable, and the rest read-only.
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return column == 0
        }

        // Ensure the first column is treated as a Boolean for the checkbox.
        override fun getColumnClass(columnIndex: Int): Class<*> {
            return if (columnIndex == 0) java.lang.Boolean::class.java else super.getColumnClass(columnIndex)
        }
    }

    init {
        title = "Select Plugins to Copy"
        init()
    }

    /**
     * Creates the central component of the dialog, which is the table of plugins.
     */
    override fun createCenterPanel(): JComponent {
        table = JBTable(dataModel)

        // Populate the table with data from the scanned plugins.
        plugins.forEach { plugin ->
            dataModel.addRow(arrayOf(true, plugin.name, plugin.version)) // Default to selected
        }

        // Configure table appearance and behavior.
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        table.columnModel.getColumn(0).maxWidth = 50 // Checkbox column
        table.columnModel.getColumn(1).preferredWidth = 350 // Name column
        table.columnModel.getColumn(2).preferredWidth = 100 // Version column

        val scrollPane = JBScrollPane(table)
        scrollPane.preferredSize = Dimension(500, 300)
        return scrollPane
    }

    /**
     * Returns the list of plugins that the user has selected via checkboxes.
     */
    fun getSelectedPlugins(): List<IdeaPluginDescriptor> {
        val selected = mutableListOf<IdeaPluginDescriptor>()
        for (i in 0 until dataModel.rowCount) {
            val isSelected = dataModel.getValueAt(i, 0) as Boolean
            if (isSelected) {
                selected.add(plugins[i])
            }
        }
        return selected
    }
}
