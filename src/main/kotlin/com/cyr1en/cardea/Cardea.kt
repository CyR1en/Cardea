package com.cyr1en.cardea

import com.cyr1en.cardea.command.CommandsBootstrapper
import com.cyr1en.cardea.dialog.DialogListener
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Cardea() : JavaPlugin() {
    override fun onEnable() {
        warnIfOffline()
        reloadJsonConfig()
        if (!dataStore.hasPassword()) {
            logger.warning("No password has been set. Please set one by using /cardea pwd <password>.")
            logger.warning("No login dialog will be shown to players until a password is set.")
        }
        Bukkit.getPluginManager().registerEvents(DialogListener(this), this)
    }

    override fun onDisable() {
        dataStore.close()
    }
}

@Suppress("UnstableApiUsage")
class CardeaBootstrapper : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        CommandsBootstrapper(context).bootstrap()
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Cardea()
    }
}
