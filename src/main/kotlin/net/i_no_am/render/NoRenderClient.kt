package net.i_no_am.render

import net.fabricmc.api.ClientModInitializer
import net.minecraft.entity.Entity
import java.io.File
import java.util.logging.Logger

object NoRenderClient : ClientModInitializer {
    val disabledEntities = mutableSetOf<String>()
    private val configFile = File("config/no-render.yml")
    val logger = Logger.getLogger("no-render")
    var enabled = true

    override fun onInitializeClient() {
        loadConfig()
        logger.info("[No Render] Loaded Config!")
        NoRenderCommand.registerCommands()
    }

    private fun loadConfig() {
        if (!configFile.exists()) {
            configFile.writeText("# Here is a list of entities that will not be rendered:\n")
        }
        disabledEntities.clear()
        disabledEntities.addAll(configFile.readLines().filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { it.trim().lowercase() })
    }

    fun saveConfig() {
        configFile.writeText("# Here is a list of entities that will not be rendered:\n")
        configFile.appendText(disabledEntities.joinToString("\n"))
    }

    fun shouldSkipRender(entity: Entity): Boolean {
        return enabled && disabledEntities.any { it.contains(entity.type.untranslatedName.toString().lowercase()) }
    }
}
