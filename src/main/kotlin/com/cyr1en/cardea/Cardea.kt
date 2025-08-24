package com.cyr1en.cardea

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.event.RegistryEvents
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.plugin.java.JavaPlugin

class Cardea() : JavaPlugin() {

    private val _dataStore = DataStore()
    val dataStore get() = _dataStore

    var jsonConfig: Config = deserialize()

    override fun onEnable() {
        logger.info("Cardea Password ${_dataStore.getPassword()}.")
        if (!_dataStore.hasPassword()) {
            logger.warning("No password has been set. Please set one by using /cardea pwd <password>.")
            logger.warning("No login dialog will be shown to players until a password is set.")
        }
        Bukkit.getPluginManager().registerEvents(CardeaListener(this), this)
    }

    companion object {
        @JvmStatic
        fun instance() = getPlugin(Cardea::class.java)
    }
}


@Suppress("UnstableApiUsage")
class CardeaBootstrapper : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(
            RegistryEvents.DIALOG.compose()
                .newHandler { e ->
                    e.registry().register(dialogKey("cardea:login")) { b ->
                        b.base(
                            DialogBase.builder(Component.text("Camp HAM HOA Login", TextColor.color(0xf38ba8)))
                                .canCloseWithEscape(false)
                                .body(
                                    mutableListOf(
                                        DialogBody.plainMessage(Component.text("To prevent unauthorized access, please enter the provided password. This will only be required once.", TextColor.color(0xcba6f7))),
                                        DialogBody.plainMessage(Component.text("Contact any HAM members if you need the password.", TextColor.color(0xbac2de))),
                                        DialogBody.plainMessage(Component.text(" ")),
                                    )
                                ).inputs(mutableListOf(
                                    DialogInput.text("password", Component.text("Enter password here:", TextColor.color(0xbac2de))).build()
                                ))
                                .build()
                        )
                        b.type(
                            DialogType.confirmation(
                                ActionButton.builder(Component.text("Enter", TextColor.color(0xEDC7FF)))
                                    .tooltip(Component.text("Click here to submit the password."))
                                    .action(DialogAction.customClick(Key.key("cardea:login/submit"), null))
                                    .build(),
                                ActionButton.builder(Component.text("Cancel", TextColor.color(0xFF8B8E)))
                                    .tooltip(Component.text("Click here to cancel the login."))
                                    .action(DialogAction.customClick(Key.key("cardea:login/cancel"), null))
                                    .build()
                            )
                        )
                    }
                }
        )

        context.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(root)
        }
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Cardea()
    }
}
