package net.i_no_am.render

import net.fabricmc.api.ClientModInitializer
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.Entity
import net.minecraft.registry.Registries
import java.io.File
import java.util.logging.Logger

object NoRenderClient : ClientModInitializer {

    val disabledEntities = mutableSetOf<String>()
    private val configFile = File("config/no-render.yml")
    val logger: Logger? = Logger.getLogger("no-render")
    var enabled = true

    override fun onInitializeClient() {
        loadConfig()
        logger?.info("[No Render] Loaded Config!")
        NoRenderCommand.registerCommands()
    }

    private fun loadConfig() {
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            configFile.writeText("# Disabled entities:\n")
        }

        disabledEntities.clear()

        disabledEntities.addAll(
            configFile.readLines()
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .map { it.trim().lowercase() }
        )
    }

    fun saveConfig() {
        val sorted = disabledEntities.toList().sorted()
        configFile.writeText("# Disabled entities:\n")
        configFile.appendText(sorted.joinToString("\n"))
    }

    // ENTITY RENDER CHECK
    fun shouldSkipRender(entity: Entity): Boolean {
        if (!enabled) return false
        val id = Registries.ENTITY_TYPE.getId(entity.type).path ?: return false
        return id.lowercase() in disabledEntities
    }

    // BLOCK ENTITY RENDER CHECK
    fun shouldSkipRender(entityType: BlockEntityType<*>): Boolean {
        if (!enabled) return false
        val id = Registries.BLOCK_ENTITY_TYPE.getId(entityType)?.path ?: return false
        return id.lowercase() in disabledEntities
    }
}
