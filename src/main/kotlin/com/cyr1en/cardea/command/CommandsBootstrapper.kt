package com.cyr1en.cardea.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.command.ConsoleCommandSender

@Suppress("UnstableApiUsage")
class CommandsBootstrapper(context: BootstrapContext) {
    private val _ctx = context
    private var _arguments = mutableListOf<CardeaCommand>()
    private val _root: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("cardea")

    init {
        _root.requires { it.sender is ConsoleCommandSender }
        _arguments.add(Password())
        _arguments.add(ShowPassword())
        _arguments.add(Invalidate())
        _arguments.add(Reload())
    }

    fun bootstrap() {
        _arguments.forEach { _root.then(it.final()) }
        val rootCmd = _root.build()

        _ctx.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(rootCmd)
        }
    }
}