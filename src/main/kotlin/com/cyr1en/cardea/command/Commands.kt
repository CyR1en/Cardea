package com.cyr1en.cardea.command

import com.cyr1en.cardea.ColorPalette.*
import com.cyr1en.cardea.dataStore
import com.cyr1en.cardea.mm
import com.cyr1en.cardea.msg
import com.cyr1en.cardea.reloadConfig
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.Server

abstract class CardeaCommand(name: String) : Command<CommandSourceStack> {

    private val _name = name
    val name get() = _name

    protected var requiredArg: RequiredArgumentBuilder<CommandSourceStack, *>? = null

    open fun final(): LiteralArgumentBuilder<CommandSourceStack> {
        if (requiredArg == null) return Commands.literal(name).executes { run(it) }
        return Commands.literal(name).then(requiredArg?.executes { run(it) })
    }
}

class Password : CardeaCommand("pwd") {
    init {
        requiredArg = Commands.argument("pwd", StringArgumentType.word())
    }

    override fun run(ctx: CommandContext<CommandSourceStack>): Int {
        val pwd = StringArgumentType.getString(ctx, "pwd")
        if (pwd.isEmpty()) {
            msg(ctx, "<color:${RED}>Password cannot be empty!</color>")
            return Command.SINGLE_SUCCESS
        }
        dataStore.removeAllLogged()
        dataStore.setPassword(pwd)
        msg(ctx, "<color:${GREEN}>Password set! All UUIDs invalidated.</color>")
        return Command.SINGLE_SUCCESS
    }
}

class ShowPassword : CardeaCommand("showpwd") {
    override fun run(ctx: CommandContext<CommandSourceStack>): Int {
        val pwd = dataStore.getPassword()
        msg(ctx, "Cardea Password is <color:${GREEN}><b>${pwd}<b></color>")
        return Command.SINGLE_SUCCESS
    }
}

class Invalidate : CardeaCommand("invalidate") {
    init {
        requiredArg = Commands.argument("player", ArgumentTypes.player())
    }

    override fun run(ctx: CommandContext<CommandSourceStack>): Int {
        val targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
        val target = targetResolver.resolve(ctx.getSource()).first()
        dataStore.removeLogged(target.uniqueId)
        ctx.source.sender.sendMessage("Invalidated ${target.uniqueId}")
        return Command.SINGLE_SUCCESS
    }
}

class Reload : CardeaCommand("reload") {
    override fun run(ctx: CommandContext<CommandSourceStack>): Int {
        reloadConfig()
        msg(ctx, "<color:${GREEN}>Reloaded config!</color>")
        return Command.SINGLE_SUCCESS
    }
}