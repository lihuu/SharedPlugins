package com.github.lihuu.sharedplugins.manager.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task


/**
 * This action is triggered from the "Tools" menu.
 * It initiates the process of scanning for third-party plugins and showing a dialog
 * for the user to select which ones to copy to the shared directory.
 */
class ScanAndCopyAction : AnAction() {

    /**
     * This method is called when the user clicks the menu item.
     */
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val centralDirPath = SharedPluginsSettings.instance.centralDirectoryPath

        // First, check if the central directory has been configured.
        if (centralDirPath.isBlank()) {
            PluginUtils.showNotification(
                project,
                "Configuration Needed",
                "Please configure the central plugins directory in Settings -> Tools -> Shared Plugins Manager.",
                NotificationType.WARNING
            )
            return
        }

        // Scan for plugins and show the selection dialog.
        val plugins = PluginUtils.scanThirdPartyPlugins()
        if (plugins.isEmpty()) {
            PluginUtils.showNotification(
                project,
                "No Plugins Found",
                "No third-party plugins were found to copy.",
                NotificationType.INFORMATION
            )
            return
        }

        val dialog = SelectPluginsDialog(plugins)
        if (dialog.showAndGet()) {
            val selectedPlugins = dialog.getSelectedPlugins()
            if (selectedPlugins.isNotEmpty()) {
                // Perform the copy operation in a background task with a progress bar.
                ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Copying Plugins") {
                    override fun run(indicator: ProgressIndicator) {
                        indicator.isIndeterminate = false
                        selectedPlugins.forEachIndexed { index, plugin ->
                            indicator.text = "Copying ${plugin.name}..."
                            indicator.fraction = (index + 1).toDouble() / selectedPlugins.size
                            PluginUtils.copyPluginToCentralDirectory(plugin, project)
                        }
                        PluginUtils.showNotification(
                            project,
                            "Copy Complete",
                            "${selectedPlugins.size} plugins were copied successfully.",
                            NotificationType.INFORMATION
                        )
                    }
                })
            }
        }
    }
}
