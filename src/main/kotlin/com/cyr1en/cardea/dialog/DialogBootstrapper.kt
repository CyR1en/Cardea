package com.cyr1en.cardea.dialog

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.registry.event.RegistryEvents

@Suppress("UnstableApiUsage")
class DialogBootstrapper(ctx: BootstrapContext) {
    private val _ctx = ctx
    private val _arguments = mutableListOf<DialogHolder>()

    init {
        _arguments.add(LoginDialogHolder())
    }

    fun bootstrap() {
        _ctx.lifecycleManager.registerEventHandler(RegistryEvents.DIALOG.compose()
            .newHandler { e ->
                _arguments.forEach { holder ->
                    e.registry().register(holder.key) { b ->
                        b.base(holder.base)
                        b.type(holder.type)
                    }
                }
            }
        )
    }
}
