package net.i_no_am.render

import net.fabricmc.api.ClientModInitializer
import net.minecraft.entity.Entity
import java.io.File
import java.util.logging.Logger

object NoRenderClient : ClientModInitializer {
    val disabledEntities = mutableSetOf<String>()
    private val configFile = File("config/no-render.yml")
    val logger = Logger.getLogger("no-render")

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
        disabledEntities.addAll(configFile.readLines().map { it.trim().lowercase() })
    }

    fun saveConfig() {
        configFile.writeText(disabledEntities.joinToString("\n"))
    }

//    fun shouldRender(entity: Entity): Boolean {
//        val entityType = entity.id.toString().lowercase()
//        return disabledEntities.none { entityType.contains(it) }
//    }
}
