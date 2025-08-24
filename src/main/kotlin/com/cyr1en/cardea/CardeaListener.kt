package com.cyr1en.cardea

import io.papermc.paper.connection.PlayerCommonConnection
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage")
class CardeaListener(plugin: Cardea) : Listener {

    private val _awaitingResponse = HashMap<PlayerCommonConnection, CompletableFuture<LoginResult>>()
    private val _plugin = plugin

    @EventHandler
    fun onPlayerConfigure(event: AsyncPlayerConnectionConfigureEvent) {
        _plugin.logger.info("Configuring ${event.connection.profile.name}.")
        _plugin.logger.info("UUID: ${event.connection.profile.id}.")
        val uuid = event.connection.profile.id ?: return
        if (_plugin.dataStore.hasLogged(uuid) || !_plugin.dataStore.hasPassword()) return

        val dialog =
            RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(Key.key("cardea:login")) ?: return
        val response = CompletableFuture<LoginResult>()

        Bukkit.getScheduler().runTaskLater(_plugin, Runnable {
            _plugin.logger.info("Timed out ${event.connection.profile.name}.")
            setConnectionJoinResult(event.connection, LoginResult.TIMEOUT)
            event.connection.audience.closeDialog()
        }, secToTicks(60))

        _awaitingResponse[event.connection] = response
        _plugin.logger.info("Awaiting response for ${event.connection.profile.name}.")
        event.connection.audience.showDialog(dialog)
        val result = response.join()
        if (result != LoginResult.CORRECT) {
            event.connection.disconnect(result.msg)
            return
        }
        _plugin.dataStore.addLogged(uuid)
        _awaitingResponse.remove(event.connection)
    }

    @EventHandler
    fun onHandleDialog(event: PlayerCustomClickEvent) {
        _plugin.logger.info("Received dialog click event.")
        _plugin.logger.info("Identifier: ${event.identifier}.")

        if (event.identifier != Key.key("cardea:login/submit")) {
            _plugin.logger.warning("Received non-login dialog click event.")
            setConnectionJoinResult(event.commonConnection, LoginResult.CANCELLED)
            return
        }

        val view = event.dialogResponseView ?: return

        val password = view.getText("password")
        if (password == null || password != _plugin.dataStore.getPassword()) {
            setConnectionJoinResult(event.commonConnection, LoginResult.INCORRECT)
            return
        }
        setConnectionJoinResult(event.commonConnection, LoginResult.CORRECT)
    }

    fun setConnectionJoinResult(connection: PlayerCommonConnection, value: LoginResult) {
        val future = _awaitingResponse[connection] ?: return
        future.complete(value)
    }
}

enum class LoginResult(val msg: Component) {
    CORRECT(Component.text("Login Successful!", TextColor.color(0xEDC7FF))),
    INCORRECT(Component.text("Incorrect Password!", TextColor.color(0xFF8B8E))),
    CANCELLED(Component.text("Login Cancelled!", TextColor.color(0xFF8B8E))),
    TIMEOUT(Component.text("Login Timeout!", TextColor.color(0xFF8B8E)));
}