package com.cyr1en.cardea

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.DialogKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun secToTicks(seconds: Int) = seconds * 20L

fun dialogKey(str: String): TypedKey<Dialog> = DialogKeys.create(Key.key(str))

fun getKey(str: String): Key = Key.key(str)

fun mm(str: String): Component = MiniMessage.miniMessage().deserialize(str);