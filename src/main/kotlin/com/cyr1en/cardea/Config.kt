package com.cyr1en.cardea

import com.cyr1en.cardea.ColorPalette.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

const val schemaUrl: String =
    "https://raw.githubusercontent.com/CyR1en/Cardea/refs/heads/master/src/main/resources/cardea-config.schema.json"

val gson: Gson = GsonBuilder()
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .create()

val defaultPath: Path = Paths.get("plugins/Cardea/config.json")

private var jsonConfig: Config = deserializeConfig()
fun cfg(): Config = jsonConfig

data class Config(
    val kickOnInvalidation: KickOnInvalidation = KickOnInvalidation(),
    val dialog: DialogConfig = DialogConfig()
)

data class DialogConfig(
    val timeout: Int = 60,
    val title: String = "<color:${MAUVE}>Cardea Login",
    val body: List<String> = listOf(
        "<color:${SUBTEXT1}>To prevent unauthorized access, please enter the server password below.",
        "",
        ""
    ),
    val inputPrompt: String = "<color:${GREEN}>Enter password here",
    val buttons: ButtonConfig = ButtonConfig(),
    val loginResult: LoginResults = LoginResults()
)

data class ButtonConfig(
    val submitLabel: String = "<color:${MAUVE}>Submit",
    val submitHover: String = "<color:${SUBTEXT1}>Click here to submit password.",
    val cancelLabel: String = "<color:${RED}>Cancel",
    val cancelHover: String = "<color:${SUBTEXT1}>Click here to cancel login."
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
        try {
            validSchema(json)
            gson.fromJson(json, Config::class.java)
        } catch (e: ValidationException) {
            logger.error("Invalid configuration!")
            logger.error(e.errorMessage)
            e.causingExceptions.map { it.message }.forEach {
                logger.error(it)
            }
            logger.error("Using default config until fixed.")
            Config()
        }
    } else {
        val defaults = Config()
        serializeConfig(
            path,
            defaults,
            schemaUrl
        )
        defaults
    }
}

/**
 * Validates a JSON string against a predefined JSON schema.
 *
 * @param json the JSON string to be validated
 * @throws [ValidationException] if [json] does not conform with the schema.
 */
fun validSchema(json: String) {
    Cardea::class.java.getResourceAsStream("/cardea-config.schema.json").use { inputStream ->
        val rawSchema = JSONObject(JSONTokener(inputStream))
        val schema: Schema = SchemaLoader.load(rawSchema)
        schema.validate(JSONObject(json))
    }
}

fun reloadJsonConfig() {
    jsonConfig = deserializeConfig()
}

