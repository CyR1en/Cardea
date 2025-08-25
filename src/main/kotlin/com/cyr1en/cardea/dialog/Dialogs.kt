package com.cyr1en.cardea.dialog

import com.cyr1en.cardea.cfg
import com.cyr1en.cardea.dialogKey
import com.cyr1en.cardea.getKey
import com.cyr1en.cardea.mm
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType


@Suppress("UnstableApiUsage")
open class DialogHolder(
    val key: TypedKey<Dialog>,
    val base: DialogBase,
    val type: DialogType
)

@Suppress("UnstableApiUsage")
class LoginDialogHolder() : DialogHolder(
    dialogKey("cardea:login"),
    DialogBase.builder(mm(cfg().dialog.title))
        .canCloseWithEscape(false)
        .body(cfg().dialog.body.map { b -> DialogBody.plainMessage(mm(b)) })
        .inputs(listOf(DialogInput.text("password", mm(cfg().dialog.inputPrompt)).build()))
        .build(),
    DialogType.confirmation(
        ActionButton.builder(mm(cfg().dialog.buttons.submitLabel))
            .tooltip(mm(cfg().dialog.buttons.submitHover))
            .action(DialogAction.customClick(getKey("cardea:login/submit"), null))
            .build(),
        ActionButton.builder(mm(cfg().dialog.buttons.cancelLabel))
            .tooltip(mm(cfg().dialog.buttons.cancelHover))
            .action(DialogAction.customClick(getKey("cardea:login/cancel"), null))
            .build()
    )
)