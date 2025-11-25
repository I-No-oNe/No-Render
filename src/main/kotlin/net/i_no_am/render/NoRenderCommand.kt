package net.i_no_am.render

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.i_no_am.render.ColorUtils.GREEN
import net.i_no_am.render.ColorUtils.PREFIX
import net.i_no_am.render.ColorUtils.RED
import net.minecraft.registry.Registries
import net.minecraft.text.Text

object NoRenderCommand {

    fun registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            register(dispatcher)
        }
    }

    private fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {

        // All valid entity + block entity names (clean, no namespace)
        val validNames: Set<String> = buildSet {
            Registries.ENTITY_TYPE.ids.forEach { add(it.path) }
            Registries.BLOCK_ENTITY_TYPE.ids.forEach { add(it.path) }
        }

        val allEntitiesSuggestion = SuggestionProvider<FabricClientCommandSource> { _, builder ->
            validNames.forEach { builder.suggest(it) }
            builder.buildFuture()
        }

        val disabledEntitiesSuggestion = SuggestionProvider<FabricClientCommandSource> { _, builder ->
            NoRenderClient.disabledEntities.forEach { builder.suggest(it) }
            builder.buildFuture()
        }

        dispatcher.register(
            ClientCommandManager.literal("no-render")

                // ---------------- ADD ----------------
                .then(
                    ClientCommandManager.literal("add")
                        .then(
                            ClientCommandManager.argument("entity", StringArgumentType.string())
                                .suggests(allEntitiesSuggestion)
                                .executes { context ->
                                    val raw = StringArgumentType.getString(context, "entity")
                                        .trim().lowercase()

                                    if (raw !in validNames) {
                                        context.source.sendFeedback(Text.of(PREFIX + RED + "'$raw' is not a valid entity"))
                                        return@executes SINGLE_SUCCESS
                                    }

                                    if (!NoRenderClient.disabledEntities.add(raw)) {
                                        context.source.sendFeedback(Text.of(PREFIX + RED + "'$raw' is already disabled"))
                                        return@executes SINGLE_SUCCESS
                                    }

                                    NoRenderClient.saveConfig()
                                    context.source.sendFeedback(Text.of("$PREFIX Added ${GREEN}$raw"))
                                    SINGLE_SUCCESS
                                }
                        )
                )

                // ---------------- REMOVE ----------------
                .then(
                    ClientCommandManager.literal("remove")
                        .then(
                            ClientCommandManager.argument("entity", StringArgumentType.string())
                                .suggests(disabledEntitiesSuggestion)
                                .executes { context ->
                                    val raw = StringArgumentType.getString(context, "entity")
                                        .trim().lowercase()

                                    if (raw !in NoRenderClient.disabledEntities) {
                                        context.source.sendFeedback(Text.of(PREFIX + RED + "'$raw' is not in the disabled list"))
                                        return@executes SINGLE_SUCCESS
                                    }

                                    NoRenderClient.disabledEntities.remove(raw)
                                    NoRenderClient.saveConfig()
                                    context.source.sendFeedback(Text.of("$PREFIX Removed ${GREEN}$raw"))
                                    SINGLE_SUCCESS
                                }
                        )
                )

                // ---------------- REMOVE ALL ----------------
                .then(
                    ClientCommandManager.literal("remove-all")
                        .executes { context ->
                            NoRenderClient.disabledEntities.clear()
                            NoRenderClient.saveConfig()
                            context.source.sendFeedback(Text.of(PREFIX + GREEN + "Cleared all disabled entities"))
                            SINGLE_SUCCESS
                        }
                )

                // ---------------- LIST ----------------
                .then(
                    ClientCommandManager.literal("list")
                        .executes { context ->
                            val list = NoRenderClient.disabledEntities

                            val msg = if (list.isEmpty())
                                PREFIX + "No entities are currently disabled."
                            else
                                PREFIX + "Disabled: " + list.joinToString(", ")

                            context.source.sendFeedback(Text.of(msg))
                            SINGLE_SUCCESS
                        }
                )

                // ---------------- TOGGLE ----------------
                .then(
                    ClientCommandManager.literal("toggle")
                        .executes { context ->
                            NoRenderClient.enabled = !NoRenderClient.enabled
                            val state = if (NoRenderClient.enabled) GREEN + "enabled" else RED + "disabled"
                            context.source.sendFeedback(Text.of(PREFIX + "No Render is now $state"))
                            SINGLE_SUCCESS
                        }
                )
        )
    }
}