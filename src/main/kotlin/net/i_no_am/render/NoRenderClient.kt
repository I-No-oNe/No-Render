package net.i_no_am.render

import net.fabricmc.api.ClientModInitializer
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.io.File
import java.util.logging.Logger

object NoRenderClient : ClientModInitializer {
    val disabledEntities = mutableSetOf<String>()
    private val configFile = File("config/no-render.yml")
    val logger: Logger = Logger.getLogger("no-render")
    var enabled = true

    override fun onInitializeClient() {
        loadConfig()
        logger.info("[No Render] Loaded Config!")
        NoRenderCommand.registerCommands()
    }

    private fun loadConfig() {
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            configFile.writeText("# Disabled entities:\n")
            return
        }
        disabledEntities.clear()
        disabledEntities += configFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { it.trim().lowercase() }
    }

    fun saveConfig() {
        configFile.writeText("# Disabled entities:\n" + disabledEntities.sorted().joinToString("\n"))
    }

    fun shouldSkipRender(entity: Entity): Boolean = enabled &&
            BuiltInRegistries.ENTITY_TYPE.getKey(entity.type).path.lowercase() in disabledEntities

    fun shouldSkipRender(entityType: BlockEntityType<*>): Boolean = enabled &&
            BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entityType)?.path?.lowercase() in disabledEntities
}