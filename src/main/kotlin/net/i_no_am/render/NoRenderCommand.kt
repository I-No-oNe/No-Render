package net.i_no_am.render

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.i_no_am.render.Global.Companion.GREEN
import net.i_no_am.render.Global.Companion.PREFIX
import net.i_no_am.render.Global.Companion.RED
import net.minecraft.registry.Registries
import net.minecraft.text.Text

object NoRenderCommand {

    fun registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            register(dispatcher)
        }
    }

    private fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        val allEntitiesSuggestion = SuggestionProvider<FabricClientCommandSource> { _, builder ->
            Registries.ENTITY_TYPE.ids.forEach { builder.suggest(it.toString()) }
            builder.buildFuture()
        }

        val disabledEntitiesSuggestion = SuggestionProvider<FabricClientCommandSource> { _, builder ->
            NoRenderClient.disabledEntities.forEach { builder.suggest(it) }
            builder.buildFuture()
        }

        dispatcher.register(
            ClientCommandManager.literal("no-render")
                .then(
                    ClientCommandManager.literal("add")
                        .then(
                            ClientCommandManager.argument("entity", StringArgumentType.greedyString())
                                .suggests(allEntitiesSuggestion)
                                .executes { context ->
                                    val entityId = StringArgumentType.getString(context, "entity").lowercase()
                                    if (NoRenderClient.disabledEntities.add(entityId)) {
                                        NoRenderClient.saveConfig()
                                        context.source.sendFeedback(Text.of(PREFIX + "Added $entityId to No Render list"))
                                    } else {
                                        context.source.sendFeedback(Text.of(PREFIX + "$entityId is already in the No Render list"))
                                    }
                                    SINGLE_SUCCESS
                                }
                        )
                )
                .then(
                    ClientCommandManager.literal("remove")
                        .then(
                            ClientCommandManager.argument("entity", StringArgumentType.string())
                                .suggests(disabledEntitiesSuggestion)
                                .executes { context ->
                                    val entityId = StringArgumentType.getString(context, "entity").lowercase()
                                    if (NoRenderClient.disabledEntities.remove(entityId)) {
                                        NoRenderClient.saveConfig()
                                        context.source.sendFeedback(Text.of(PREFIX + "Removed $entityId from No Render list"))
                                    } else {
                                        context.source.sendFeedback(Text.of(PREFIX + "$entityId is not in the No Render list"))
                                    }
                                    SINGLE_SUCCESS
                                }
                        )
                )
                .then(
                    ClientCommandManager.literal("list")
                        .executes { context ->
                            val entityList = if (NoRenderClient.disabledEntities.isEmpty()) {
                                PREFIX + "No entities are currently disabled."
                            } else {
                                PREFIX + "Disabled Entities: " + NoRenderClient.disabledEntities.joinToString(", ")
                            }
                            context.source.sendFeedback(Text.of(entityList))
                            SINGLE_SUCCESS
                        }
                )
                .then(
                    ClientCommandManager.literal("toggle")
                        .executes { context ->
                            NoRenderClient.enabled = !NoRenderClient.enabled
                            context.source.sendFeedback(Text.of(PREFIX + "No Render is now " + if (NoRenderClient.enabled) GREEN + "enabled" else RED + "disabled"))
                            SINGLE_SUCCESS
                        }
                )
        )
    }
}
