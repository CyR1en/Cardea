package com.cyr1en.cardea.command

import com.cyr1en.cardea.ColorPalette.*
import com.cyr1en.cardea.cfg
import com.cyr1en.cardea.dataStore
import com.cyr1en.cardea.mm
import com.cyr1en.cardea.msg
import com.cyr1en.cardea.reloadJsonConfig
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit

abstract class CardeaCommand(
    name: String,
) : Command<CommandSourceStack> {
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
            msg(ctx, "<color:$RED>Password cannot be empty!</color>")
            return Command.SINGLE_SUCCESS
        }

        dataStore.setPassword(pwd)
        msg(ctx, "<color:$GREEN>Password set to <color:$MAUVE>$pwd</color>!</color>")
        if (cfg().kickOnInvalidation.enabled) {
            dataStore.removeAllLogged()
            msg(ctx, "<color:$GREEN>All UUIDs invalidated.</color>")
        }
        return Command.SINGLE_SUCCESS
    }
}

class ShowPassword : CardeaCommand("showpwd") {
    override fun run(ctx: CommandContext<CommandSourceStack>): Int {
        val pwd = dataStore.getPassword()
        msg(ctx, "Cardea Password is <color:$GREEN><b>$pwd<b></color>")
        return Command.SINGLE_SUCCESS
    }
}

class Invalidate : CardeaCommand("invalidate") {
    init {
        requiredArg =
            Commands
                .argument("target", StringArgumentType.greedyString())
                .suggests { ctx, builder ->
                    val names = dataStore.getLoggedUsernames()
                    names.forEach { builder.suggest(it) }
                    if (!names.isEmpty()) builder.suggest("all")
                    return@suggests builder.buildFuture()
                }
    }

    override fun run(ctx: CommandContext<CommandSourceStack>): Int {
        val target = StringArgumentType.getString(ctx, "target")
        if (target == "all") {
            dataStore.removeAllLogged()
            msg(ctx, "<color:$GREEN>All UUIDs invalidated!</color>")

            if (cfg().kickOnInvalidation.enabled) {
                Bukkit.getOnlinePlayers().forEach { it.kick(mm(cfg().kickOnInvalidation.message)) }
            }

            return Command.SINGLE_SUCCESS
        }
        if (!dataStore.hasLogged(target)) {
            msg(ctx, "<color:$RED>Player has never logged in!</color>")
            return Command.SINGLE_SUCCESS
        }
        dataStore.removeLogged(target)
        msg(ctx, "Invalidated <color:$GREEN><b>$target</b></color>!")
        if (cfg().kickOnInvalidation.enabled) {
            Bukkit.getPlayer(target)?.kick(mm(cfg().kickOnInvalidation.message))
        }
        return Command.SINGLE_SUCCESS
    }
}

class Reload : CardeaCommand("reload") {
    override fun run(ctx: CommandContext<CommandSourceStack>): Int {
        reloadJsonConfig()
        msg(ctx, "<color:$GREEN>Reloaded config!</color>")
        return Command.SINGLE_SUCCESS
    }
}
