package com.cyr1en.cardea

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

val gson: Gson = GsonBuilder()
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .create()

val defaultPath: Path = Paths.get("plugins/Cardea/config.json")

data class Config(
    val title: String = "<red>Login Dialog</red>",
    val body: List<String> = listOf(),
    val inputPrompt: String = "Enter password here",
    val buttons: ButtonConfig = ButtonConfig()
)

data class ButtonConfig(
    val submitLabel: String = "Submit",
    val submitHover: String = "Click here to submit password.",
    val cancelLabel: String = "Cancel",
    val cancelHover: String = "Click here to cancel login."
)

fun serialize(path: Path = defaultPath, config: Config) {
    val json = gson.toJson(config)
    Files.createDirectories(path.parent)
    Files.writeString(
        path, json,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    )
}

fun deserialize(path: Path = defaultPath): Config {
    return if (Files.exists(path)) {
        val json = Files.readString(path)
        gson.fromJson(json, Config::class.java)
    } else {
        val defaults = Config()
        serialize(path, defaults)
        defaults
    }
}

