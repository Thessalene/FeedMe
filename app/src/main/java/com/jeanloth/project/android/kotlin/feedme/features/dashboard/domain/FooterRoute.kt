package com.jeanloth.project.android.kotlin.feedme.features.dashboard.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.DialogType

enum class FooterRoute(val route: String, val title: String? =null, val icon: ImageVector? = null, val inFooter: Boolean = false, val actionButton: Boolean = false, val displayFooter : Boolean = true, val displayBackOrClose : Boolean = false, val displayAddButton : Boolean = false, val dialogType: DialogType? = null) {
    HOME("home", icon = Icons.Filled.Home, inFooter = true, displayFooter = true),
    COMMAND_LIST("command_list", title = "Commandes", icon = Icons.Filled.List, inFooter = true, displayFooter = true),
    ADD_COMMAND_BUTTON("add_command", title = "Création commande", Icons.Filled.Add, true, true, false, displayBackOrClose = true),
    CLIENT("clients", title = "Clients", Icons.Filled.Person, true, displayFooter = true, dialogType = DialogType.ADD_CLIENT, displayAddButton = true),
    PRODUCTS("products", title = "Paniers", Icons.Filled.ShoppingCart, true, displayFooter = false, displayBackOrClose = true, displayAddButton = true),

    // Not in footer
    ADD_CLIENT("add_client", title = "Création client", displayFooter = false, displayBackOrClose = true),
    ADD_COMMAND("add_command", title = "Création commande", displayFooter = false, displayBackOrClose = true),
    ADD_PRODUCTS("add_products", title = "Paniers", Icons.Filled.ShoppingCart, false, displayFooter = false, displayBackOrClose = true);

    companion object{
        fun fromVal(route: String?) = values()
            .firstOrNull { it.route == route } ?: HOME
    }
}