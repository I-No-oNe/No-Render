package net.i_no_am.render

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.i_no_am.render.ColorUtils.GREEN
import net.i_no_am.render.ColorUtils.PREFIX
import net.i_no_am.render.ColorUtils.RED
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component.literal as text

object NoRenderCommand {

    fun registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ -> register(dispatcher) }
    }

    private fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        val validNames = BuiltInRegistries.ENTITY_TYPE.keySet().map { it.path }.toSet() +
                BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet().map { it.path }.toSet()

        val allEntitiesSuggestion = SuggestionProvider<FabricClientCommandSource> { _, b ->
            validNames.forEach { b.suggest(it) }; b.buildFuture()
        }
        val disabledEntitiesSuggestion = SuggestionProvider<FabricClientCommandSource> { _, b ->
            NoRenderClient.disabledEntities.forEach { b.suggest(it) }; b.buildFuture()
        }

        dispatcher.register(
            literal("no-render")
                .then(literal("add").then(argument("entity", string()).suggests(allEntitiesSuggestion).executes { ctx ->
                    val raw = getString(ctx, "entity").trim().lowercase()
                    val src = ctx.source

                    when {
                        raw !in validNames -> src.sendFeedback(text("$PREFIX$RED'$raw' is not a valid entity"))
                        !NoRenderClient.disabledEntities.add(raw) -> src.sendFeedback(text("$PREFIX$RED'$raw' is already disabled"))
                        else -> {
                            NoRenderClient.saveConfig()
                            src.sendFeedback(text("$PREFIX Added $GREEN$raw"))
                        }
                    }
                    SINGLE_SUCCESS
                }))
                .then(literal("remove").then(argument("entity", string()).suggests(disabledEntitiesSuggestion).executes { ctx ->
                    val raw = getString(ctx, "entity").trim().lowercase()
                    val src = ctx.source

                    if (raw !in NoRenderClient.disabledEntities) {
                        src.sendFeedback(text("$PREFIX$RED'$raw' is not in the disabled list"))
                    } else {
                        NoRenderClient.disabledEntities.remove(raw)
                        NoRenderClient.saveConfig()
                        src.sendFeedback(text("$PREFIX Removed $GREEN$raw"))
                    }
                    SINGLE_SUCCESS
                }))
                .then(literal("remove-all").executes { ctx ->
                    NoRenderClient.disabledEntities.clear()
                    NoRenderClient.saveConfig()
                    ctx.source.sendFeedback(text("$PREFIX${GREEN}Cleared all disabled entities"))
                    SINGLE_SUCCESS
                })
                .then(literal("list").executes { ctx ->
                    val list = NoRenderClient.disabledEntities
                    val msg = if (list.isEmpty()) "No entities are currently disabled." else "Disabled: ${list.joinToString(", ")}"
                    ctx.source.sendFeedback(text(PREFIX + msg))
                    SINGLE_SUCCESS
                })
                .then(literal("toggle").executes { ctx ->
                    NoRenderClient.enabled = !NoRenderClient.enabled
                    val state = if (NoRenderClient.enabled) "${GREEN}enabled" else "${RED}disabled"
                    ctx.source.sendFeedback(text("${PREFIX}No Render is now $state"))
                    SINGLE_SUCCESS
                })
        )
    }
}