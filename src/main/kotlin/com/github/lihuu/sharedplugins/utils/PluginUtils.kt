package com.github.lihuu.sharedplugins.utils

import com.github.lihuu.sharedplugins.manager.services.SharedPluginsSettings
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


object PluginUtils {

    private val LOG = logger<PluginUtils>()
    private const val NOTIFICATION_GROUP_ID = "SharedPluginsManager"

    /**
     * Scans and returns a list of all installed third-party plugins.
     * It filters out the plugins that are bundled with the IDE.
     */
    fun scanThirdPartyPlugins(): List<IdeaPluginDescriptor> {
        return PluginManagerCore.plugins.filter { !it.isBundled && it.isEnabled }
    }

    /**
     * Copies a single plugin's files to the configured central directory.
     * @param plugin The plugin descriptor of the plugin to copy.
     * @param project The current project, used for displaying notifications.
     */
    fun copyPluginToCentralDirectory(plugin: IdeaPluginDescriptor, project: Project?) {
        val centralDirPath = SharedPluginsSettings.instance.centralDirectoryPath
        if (centralDirPath.isBlank()) {
            showNotification(
                project,
                "Central directory not set",
                "Please set the central directory in Settings -> Tools -> Shared Plugins Manager.",
                NotificationType.WARNING
            )
            return
        }

        val centralDir = Paths.get(centralDirPath)
        if (!Files.exists(centralDir)) {
            try {
                Files.createDirectories(centralDir)
            } catch (e: IOException) {
                LOG.error("Failed to create central directory: $centralDirPath", e)
                showNotification(
                    project,
                    "Error",
                    "Failed to create central directory: ${e.message}",
                    NotificationType.ERROR
                )
                return
            }
        }

        val pluginPath: Path = plugin.pluginPath ?: return
        val targetPath = centralDir.resolve(pluginPath.fileName)

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                Files.copy(pluginPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
                LOG.info("Successfully copied plugin '${plugin.name}' to '$targetPath'")
                showNotification(
                    project,
                    "Plugin Copied",
                    "Plugin '${plugin.name}' was copied to the shared directory.",
                    NotificationType.INFORMATION
                )
            } catch (e: Exception) {
                LOG.error("Failed to copy plugin '${plugin.name}' from '$pluginPath' to '$targetPath'", e)
                showNotification(
                    project,
                    "Copy Failed",
                    "Failed to copy plugin '${plugin.name}': ${e.message}",
                    NotificationType.ERROR
                )
            }
        }
    }

    /**
     * A helper function to display notifications to the user.
     */
    fun showNotification(project: Project?, title: String, content: String, type: NotificationType) {
        ApplicationManager.getApplication().invokeLater {
            val notification = Notification(NOTIFICATION_GROUP_ID, title, content, type)
            Notifications.Bus.notify(notification, project)
        }
    }
}