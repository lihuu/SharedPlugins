package com.github.lihuu.sharedplugins.manager.settings

import com.github.lihuu.sharedplugins.manager.services.SharedPluginsSettings
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/**
 * Provides the user interface for the plugin's settings page in the IDE's Settings/Preferences dialog.
 * It allows users to view and edit the central shared plugins directory path.
 */
class SharedPluginsSettingsConfigurable : Configurable {

    private lateinit var settingsPanel: JComponent
    private lateinit var centralDirectoryField: TextFieldWithBrowseButton
    private val settings = SharedPluginsSettings.instance

    /**
     * Creates the main Swing component for the settings page.
     * This method is called by the IDE to display the settings UI.
     */
    override fun createComponent(): JComponent {
        centralDirectoryField = TextFieldWithBrowseButton()
        // Add a file chooser to the browse button
        centralDirectoryField.addBrowseFolderListener(
            "Select Central Plugin Directory",
            "Please select the directory where shared plugins will be stored.",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )

        // Use the Kotlin UI DSL to build a clean and modern layout.
        settingsPanel = panel {
            row("Central Directory:") {
                cell(centralDirectoryField)
                    .resizableColumn()
                    .comment("The directory to store shared plugins.")
            }
        }
        return settingsPanel
    }

    /**
     * Checks if the settings have been modified by the user.
     * The "Apply" button is enabled only if this method returns true.
     */
    override fun isModified(): Boolean {
        return centralDirectoryField.text != settings.centralDirectoryPath
    }

    /**
     * Saves the modified settings.
     * This method is called when the user clicks "Apply" or "OK".
     */
    override fun apply() {
        settings.centralDirectoryPath = centralDirectoryField.text
    }

    /**
     * Resets the UI to reflect the currently stored settings.
     * This method is called when the user clicks "Reset" or when the dialog is first opened.
     */
    override fun reset() {
        centralDirectoryField.text = settings.centralDirectoryPath
    }

    /**
     * The name of the settings page as it appears in the settings tree.
     */
    override fun getDisplayName(): String {
        return "Shared Plugins Manager"
    }
}