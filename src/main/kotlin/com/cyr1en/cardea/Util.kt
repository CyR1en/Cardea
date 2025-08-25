package com.cyr1en.cardea

import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.DialogKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

fun secToTicks(seconds: Int) = seconds * 20L

fun dialogKey(str: String): TypedKey<Dialog> = DialogKeys.create(Key.key(str))

fun getKey(str: String): Key = Key.key(str)

fun mm(str: String): Component = MiniMessage.miniMessage().deserialize(str);

fun msg(sender: CommandSender, msg: String) =
    sender.sendMessage(
        mm(msg)
    )

fun msg(ctx: CommandContext<CommandSourceStack>, msg: String) = msg(ctx.source.sender, msg)