package com.cyr1en.cardea

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
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
    val kickOnInvalidation: KickOnInvalidation = KickOnInvalidation(),
    val dialog: DialogConfig = DialogConfig()
)

data class DialogConfig(
    val timeout: Int = 60,
    val title: String = "<color:#ca9ee6>Cardea Login",
    val body: List<String> = listOf("<color:#a5adce>To prevent unauthorized access, please enter the server password below.", "", ""),
    val inputPrompt: String = "<color:#a6d189>Enter password here",
    val buttons: ButtonConfig = ButtonConfig(),
    val loginResult: LoginResults = LoginResults()
)

data class ButtonConfig(
    val submitLabel: String = "<color:#ca9ee6>Submit",
    val submitHover: String = "<color:#838ba7>Click here to submit password.",
    val cancelLabel: String = "<color:#e78284>Cancel",
    val cancelHover: String = "<color:#838ba7>Click here to cancel login."
)

data class LoginResults(
    val success: String = "<green>Login Successful!</green>",
    val incorrect: String = "<red>Incorrect Password!</red>",
    val cancelled: String = "<red>Login Cancelled!</red>",
    val timeout: String = "<red>Login Timeout!</red>"
)

data class KickOnInvalidation(
    val enabled: Boolean = true,
    val message: String = "<color:${RED}>Your login for this server has been invalidated.</color>"
)

fun serializeConfig(path: Path = defaultPath, config: Config, schemaUrl: String? = null) {
    val configObj = gson.toJsonTree(config).asJsonObject
    val root = if (schemaUrl != null) {
        val withSchema = JsonObject()
        withSchema.addProperty($$"$schema", schemaUrl)
        configObj.entrySet().forEach { (k, v) -> withSchema.add(k, v) }
        withSchema
    } else configObj

    val json = gson.toJson(root)
    Files.createDirectories(path.parent)
    Files.writeString(
        path, json,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    )
}

fun deserializeConfig(path: Path = defaultPath): Config {
    return if (Files.exists(path)) {
        val json = Files.readString(path)
        gson.fromJson(json, Config::class.java)
    } else {
        val defaults = Config()
        serializeConfig(
            path,
            defaults,
            "https://raw.githubusercontent.com/CyR1en/Cardea/refs/heads/master/src/main/resources/cardea-config.schema.json"
        )
        defaults
    }
}

fun reloadConfig() {
    jsonConfig = deserializeConfig()
}

private var jsonConfig: Config = deserializeConfig()
fun cfg(): Config = jsonConfig

