package com.github.lihuu.sharedplugins.manager.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Manages the persistent state of the plugin's settings.
 * This class is responsible for storing the path to the central shared plugins directory.
 * It uses the PersistentStateComponent to save the state in an XML file.
 */
@State(
    name = "com.sharedplugins.manager.settings.SharedPluginsSettings",
    storages = [Storage("SharedPluginsManagerSettings.xml")]
)
class SharedPluginsSettings : PersistentStateComponent<SharedPluginsSettings.State> {

    // The internal state object that holds the settings data.
    private var internalState = State()

    // Data class to hold the actual settings values.
    // This makes the state easily serializable to XML.
    class State {
        var centralDirectoryPath: String = ""
    }

    companion object {
        // Provides a convenient way to get an instance of this settings service.
        val instance: SharedPluginsSettings
            get() = ApplicationManager.getApplication().getService(SharedPluginsSettings::class.java)
    }

    // Returns the current state of the component.
    // The framework will serialize this state to XML.
    override fun getState(): State {
        return internalState
    }

    // Loads the state of the component from the XML file.
    // This method is called by the framework when the component is initialized.
    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, internalState)
    }

    // A property to easily access and modify the central directory path from other parts of the plugin.
    var centralDirectoryPath: String
        get() = internalState.centralDirectoryPath+"/SharedPlugins"
        set(value) {
            internalState.centralDirectoryPath = value
        }
}