package com.cyr1en.cardea.dialog

import com.cyr1en.cardea.Cardea
import com.cyr1en.cardea.cfg
import com.cyr1en.cardea.dataStore
import com.cyr1en.cardea.getKey
import com.cyr1en.cardea.mm
import com.cyr1en.cardea.secToTicks
import io.papermc.paper.connection.PlayerCommonConnection
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage")
class DialogListener(plugin: Cardea) : Listener {

    private val logger = ComponentLogger.logger(this::class.java)
    private val _awaitingResponse = HashMap<PlayerCommonConnection, CompletableFuture<LoginResult>>()
    private val _plugin = plugin

    @EventHandler
    fun onPlayerConfigure(event: AsyncPlayerConnectionConfigureEvent) {
        val uuid = event.connection.profile.id ?: return
        if (dataStore.hasLogged(uuid) || !dataStore.hasPassword()) return

        val dialog =
            RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(Key.key("cardea:login")) ?: return
        val response = CompletableFuture<LoginResult>()

        Bukkit.getScheduler().runTaskLater(_plugin, Runnable {
            if(!_awaitingResponse.containsKey(event.connection)) return@Runnable
            setConnectionJoinResult(event.connection, LoginResult.TIMEOUT)
            event.connection.audience.closeDialog()
            _awaitingResponse.remove(event.connection)
        }, secToTicks(cfg().dialog.timeout))

        _awaitingResponse[event.connection] = response
        event.connection.audience.showDialog(dialog)
        val result = response.join()
        if (result != LoginResult.CORRECT) {
            event.connection.disconnect(result.msg)
            _awaitingResponse.remove(event.connection)
        } else {
            dataStore.addLogged(uuid)
            _awaitingResponse.remove(event.connection)
        }
    }

    @EventHandler
    fun onHandleDialog(event: PlayerCustomClickEvent) {
        val identifier = event.identifier
        logger.info("Received dialog click: $identifier")
        if (identifier != getKey("cardea:login/submit") && identifier != getKey("cardea:login/cancel")) {
            logger.info("Invalid identifier: $identifier")
            return
        }

        if (identifier == getKey("cardea:login/cancel")) {
            logger.info("Login cancelled.")
            setConnectionJoinResult(event.commonConnection, LoginResult.CANCELLED)
            return
        }

        val view = event.dialogResponseView ?: return

        val password = view.getText("password")
        if (password == null || password != dataStore.getPassword()) {
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
    CORRECT(mm(cfg().dialog.loginResult.success)),
    INCORRECT(mm(cfg().dialog.loginResult.incorrect)),
    CANCELLED(mm(cfg().dialog.loginResult.cancelled)),
    TIMEOUT(mm(cfg().dialog.loginResult.timeout));
}