package com.jeanloth.project.android.kotlin.feedme

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jeanloth.project.android.kotlin.feedme.core.theme.FeedMeTheme
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.AddButtonActionType
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.*
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.basket.BasketVM
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.client.AddClientPage
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.client.ClientListPage
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.client.ClientVM
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.command.CommandVM
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.common.YesNoDialog
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.common.client.PageTemplate
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.data.CreateCommandCallbacks
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.data.CreateCommandParameters
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.products.ProductVM
import com.jeanloth.project.android.kotlin.feedme.features.dashboard.domain.FooterRoute
import com.jeanloth.project.android.kotlin.feedme.features.dashboard.domain.FooterRoute.Companion.fromVal
import com.jeanloth.project.android.kotlin.feedme.features.dashboard.presentation.HomePage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import splitties.toast.UnreliableToastApi

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val clientVM : ClientVM by viewModels()
    private val productVM : ProductVM by viewModels()
    private val basketVM : BasketVM by viewModels()
    private val commandVM : CommandVM by viewModels()

    @OptIn(ExperimentalComposeUiApi::class, UnreliableToastApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        productVM.syncProducts()

        lifecycleScope.launchWhenCreated {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                commandVM.basketWrappers.collect{
                    Log.d("Command", "Wrappers received : $it")
                }
            }
        }

        setContent {

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            val currentRoute = rememberSaveable {
                mutableStateOf(fromVal(navBackStackEntry?.destination?.route))
            }
            val topBarState = rememberSaveable { (mutableStateOf(true)) }
            val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
            val displayBackOrCloseState = rememberSaveable { (mutableStateOf(false)) }
            val displayAddButtonState = rememberSaveable { (mutableStateOf(false)) }
            val dialogType = rememberSaveable { (mutableStateOf<AddButtonActionType?>(null)) }
            val scope = rememberCoroutineScope()
            val displayYesNoDialog = remember { mutableStateOf(false) }

            val selectedClient by commandVM.client.collectAsState()
            val clients by clientVM.allSF.collectAsState()
            val basketItems by productVM.basketItems.collectAsState()
            val baskets by basketVM.baskets.collectAsState()
            val basketWrappers by commandVM.basketWrappers.collectAsState()
            val productWrappers by commandVM.productWrappers.collectAsState()
            val commands by commandVM.commands.collectAsState()
            val currentCommand by commandVM.currentCommand.collectAsState()

            val title = fromVal(navBackStackEntry?.destination?.route).title
            topBarState.value = fromVal(navBackStackEntry?.destination?.route).title != null
            bottomBarState.value = fromVal(navBackStackEntry?.destination?.route).displayFooter
            displayBackOrCloseState.value = fromVal(navBackStackEntry?.destination?.route).displayBackOrClose
            displayAddButtonState.value = fromVal(navBackStackEntry?.destination?.route).displayAddButton
            dialogType.value = fromVal(navBackStackEntry?.destination?.route).dialogType

            val keyboardController = LocalSoftwareKeyboardController.current

            if(displayYesNoDialog.value){
                YesNoDialog(
                    onYesClicked = {
                        displayYesNoDialog.value = false
                        navController.navigate(FooterRoute.COMMAND_LIST.route)
                        splitties.toast.toast(R.string.current_command_saved)
                    },
                    onNoClicked = {
                        commandVM.clearCurrentCommand()
                        displayYesNoDialog.value = false
                        navController.navigate(FooterRoute.COMMAND_LIST.route)
                    }
                )
            }

            FeedMeTheme {
                PageTemplate(
                    context = this,
                    title  = title,
                    navController = navController,
                    displayHeader = topBarState.value,
                    displayBottomNav = bottomBarState.value,
                    displayBackOrClose = displayBackOrCloseState.value ,
                    displayAddButton = displayAddButtonState.value ,
                    currentRoute = currentRoute.value,
                    onCloseOrBackClick = {
                        keyboardController?.hide()
                        when(navController.currentDestination?.route){
                            FooterRoute.ADD_BASKET.route -> navController.popBackStack(FooterRoute.BASKETS.route, false)
                            FooterRoute.ADD_COMMAND_BUTTON.route, FooterRoute.ADD_COMMAND.route -> {
                                // Display dialog to know if the user want to keep current command saved to edit it later
                                if(commandVM.canAskUserToSaveCommand()) displayYesNoDialog.value = true else {
                                    navController.navigate(FooterRoute.COMMAND_LIST.route)
                                }
                            }
                            else -> navController.popBackStack(FooterRoute.HOME.route, false)
                        }
                     },
                    onDialogDismiss = { keyboardController?.hide() },
                    addButtonActionType = dialogType.value,
                    content = {
                        NavHost(navController = navController, startDestination = FooterRoute.HOME.route, modifier = Modifier.padding(15.dp)) {
                            // Home page
                            composable(FooterRoute.HOME.route) { HomePage(navController) }

                            // List of commands page
                            composable(FooterRoute.COMMAND_LIST.route) { CommandListPage(commands, onClick = {
                                commandVM.updateCurrentCommand(it)
                                navController.navigate(FooterRoute.COMMAND_DETAIL.route)
                            }) }

                            // Detail of command
                            composable(FooterRoute.COMMAND_DETAIL.route) { navBackStackEntry ->
                                CommandDetailPage(currentCommand)
                            }

                            // Add command page
                            composable(FooterRoute.ADD_COMMAND_BUTTON.route) {
                                AddCommandPage(
                                    parameters = CreateCommandParameters(
                                        clients = clients,
                                        selectedClient = selectedClient,
                                        basketWrappers = basketWrappers,
                                        productWrappers = productWrappers
                                    ),
                                    callbacks = CreateCommandCallbacks (
                                        onNewClientAdded = clientVM::saveClient,
                                        onBasketQuantityChange = commandVM::setBasketQuantityChange,
                                        onProductQuantityChange = commandVM::setProductQuantityChange,
                                        onClientSelected = commandVM::updateClient,
                                        onCommandPriceSelected = commandVM::updateCommandPrice,
                                        onDateChanged = commandVM::updateDeliveryDate,
                                        onCreateCommandClick = {
                                            navController.navigate(FooterRoute.COMMAND_LIST.route)  // Navigate back to command list page
                                            val isSavingSuccess = commandVM.saveCommand()
                                            Toast.makeText(this@MainActivity, if(isSavingSuccess)  "Votre commande a été enregistrée" else "Une erreur est survenue", Toast.LENGTH_SHORT)
                                        }
                                    )
                                )
                            }

                            // Client list page
                            composable(FooterRoute.CLIENT.route) { ClientListPage(clientVM, onClientRemoved = clientVM::removeClient) }

                            // Basket list page
                            composable(FooterRoute.BASKETS.route){ BasketList(baskets) }

                            // Add basket page
                            composable(FooterRoute.ADD_BASKET.route) {
                                CreateBasketPage(
                                    basketItems = basketItems,
                                    onValidateBasket = { label, price, productQuantity ->
                                        scope.launch {
                                            if(basketVM.saveBasket(label, price, productQuantity)) {
                                                splitties.toast.toast(R.string.basket_added)
                                            } else {
                                                splitties.toast.toast(R.string.basket_no_added)
                                            }
                                            navController.navigate(FooterRoute.BASKETS.route)
                                        }
                                    },
                                    onAddProduct = productVM::saveProduct
                                )
                            }

                            // Add command page - Not in footer
                            composable(FooterRoute.ADD_COMMAND.route) {
                                AddCommandPage(
                                    parameters = CreateCommandParameters(
                                        clients = clients,
                                        selectedClient = selectedClient,
                                        basketWrappers = basketWrappers,
                                        productWrappers = productWrappers
                                    ),
                                    callbacks = CreateCommandCallbacks (
                                        onNewClientAdded = clientVM::saveClient,
                                        onBasketQuantityChange = commandVM::setBasketQuantityChange,
                                        onProductQuantityChange = commandVM::setProductQuantityChange,
                                        onClientSelected = commandVM::updateClient,
                                        onCommandPriceSelected = commandVM::updateCommandPrice,
                                        onDateChanged = commandVM::updateDeliveryDate,
                                        onCreateCommandClick = {
                                            navController.navigate(FooterRoute.COMMAND_LIST.route)  // Navigate back to command list page
                                            val isSavingSuccess = commandVM.saveCommand()
                                            Toast.makeText(this@MainActivity, if(isSavingSuccess)  "Votre commande a été enregistrée" else "Une erreur est survenue", Toast.LENGTH_SHORT)
                                        }
                                    )
                                )
                            }
                            // Add client page - Not in footer
                            composable(FooterRoute.ADD_CLIENT.route) {
                                AddClientPage(
                                    onValidateClick = { Toast.makeText(this@MainActivity, "Clic sur valider", Toast.LENGTH_SHORT) }
                                )
                            }
                        }
                    },
                    onNewClientAdded = clientVM::saveClient
                )
            }
        }
    }
}
