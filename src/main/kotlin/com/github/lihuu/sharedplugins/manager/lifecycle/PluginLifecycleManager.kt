package com.github.lihuu.sharedplugins.manager.lifecycle

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.github.lihuu.sharedplugins.manager.listeners.PluginInstallListener

/**
 * This class is registered as a `postStartupActivity` extension.
 * Its purpose is to programmatically subscribe to application-level events
 * after the IDE has fully started and a project is opened.
 */
class PluginLifecycleManager : StartupActivity {

    /**
     * This method is executed by the IDE framework on project startup.
     * We use it to connect our custom listener to the application's message bus.
     *
     * @param project The project that has just been opened.
     */
    override fun runActivity(project: Project) {
        // The message bus connection is automatically disposed when the project is closed,
        // preventing memory leaks.
        val connection = project.messageBus.connect()

        // Subscribe our PluginInstallListener to the DynamicPluginListener.TOPIC.
        // From now on, whenever a plugin is loaded or unloaded, our listener's methods will be called.
        connection.subscribe(DynamicPluginListener.TOPIC, PluginInstallListener())
    }
}