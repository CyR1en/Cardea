package com.cyr1en.cardea

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import org.bukkit.command.ConsoleCommandSender

private val subPwd: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("pwd").then(
    Commands.literal("pwd")
        .then(
            Commands.argument("pwd", StringArgumentType.word())
                .executes { ctx ->
                    val pwd = StringArgumentType.getString(ctx, "pwd")
                    if (pwd.isEmpty()) {
                        ctx.source.sender.sendMessage(Component.text("Password cannot be empty!"))
                        return@executes Command.SINGLE_SUCCESS
                    }
                    val instance = Cardea.instance()
                    instance.logger.info("Setting password to $pwd.")
                    Cardea.instance().dataStore.setPassword(pwd)
                    ctx.source.sender.sendMessage(Component.text("Password set successfully!"))
                    return@executes Command.SINGLE_SUCCESS
                }
        ))

private val subShowPwd: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("showpwd").then(
    Commands.literal("showpwd")
        .executes { ctx ->
            val instance = Cardea.instance()
            ctx.source.sender.sendMessage(Component.text("Password: ${instance.dataStore.getPassword()}"))
            return@executes Command.SINGLE_SUCCESS
        }
)

private val subInvalidate: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("Invalidate").then(
    Commands.literal("invalidate")
        .requires { sender -> sender.sender is ConsoleCommandSender }
        .then(
            Commands.argument("player", ArgumentTypes.player())
                .executes { ctx ->
                    val targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                    val target = targetResolver.resolve(ctx.getSource()).first()
                    Cardea.instance().dataStore.removeLogged(target.uniqueId)
                    ctx.source.sender.sendMessage("Invalidated ${target.uniqueId}")
                    return@executes Command.SINGLE_SUCCESS
                }
        )
)

private val subReload: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("reload").executes { ctx ->
    val instance = Cardea.instance()
    instance.jsonConfig = deserialize()
    return@executes Command.SINGLE_SUCCESS
}

val root: LiteralCommandNode<CommandSourceStack> = Commands.literal("cardea")
    .requires { sender -> sender.sender is ConsoleCommandSender }
    .then(subPwd)
    .then(subShowPwd)
    .then(subInvalidate)
    .then(subReload)
    .build()