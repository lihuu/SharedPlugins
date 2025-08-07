package com.github.lihuu.sharedplugins.manager.listeners

import com.github.lihuu.sharedplugins.manager.services.SharedPluginsSettings
import com.github.lihuu.sharedplugins.utils.PluginUtils
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.ProjectManager


/**
 * Listens for dynamic plugin installation events.
 * When a new third-party plugin is installed and loaded, this listener prompts the user
 * to ask if they want to copy it to the shared directory.
 */
class PluginInstallListener : DynamicPluginListener {

    /**
     * This method is called by the IDE framework when a plugin is loaded.
     * This includes initial startup and when a new plugin is installed dynamically.
     */
    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        // We only care about non-bundled plugins that are not this plugin itself.
        if (pluginDescriptor.isBundled || pluginDescriptor.pluginId.idString == "com.your.company.sharedpluginsmanager") {
            return
        }

        // Also check if the central directory is configured. If not, do nothing.
        if (SharedPluginsSettings.instance.centralDirectoryPath.isBlank()) {
            return
        }

        // Get a project reference to show the notification.
        // It's okay if it's null, notifications can be application-level.
        val project = ProjectManager.getInstance().openProjects.firstOrNull()

        // Create and show a notification with "Yes" and "No" actions.
        val notificationGroup = com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("SharedPluginsManager")
        val notification = notificationGroup.createNotification(
            "New Plugin Installed",
            "Copy '${pluginDescriptor.name}' to the shared directory?",
            NotificationType.INFORMATION
        )

        // Define the "Yes" action.
        notification.addAction(NotificationAction.createSimple("Yes") {
            PluginUtils.copyPluginToCentralDirectory(pluginDescriptor, project)
            notification.expire() // Close the notification after action
        })

        // Define the "No" action.
        notification.addAction(NotificationAction.createSimple("No") {
            notification.expire()
        })

        Notifications.Bus.notify(notification, project)
    }
}