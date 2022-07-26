package com.jeanloth.project.android.kotlin.feedme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jeanloth.project.android.kotlin.feedme.core.theme.FeedMeTheme
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.AddButtonActionType
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.*
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.basket.BasketVM
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.basket.CreateBasketPage
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.client.AddClientPage
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.client.ClientListPage
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.client.ClientVM
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.command.CommandDetailPage
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.command.CommandVM
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.common.client.PageTemplate
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.data.CreateCommandCallbacks
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.data.CreateCommandParameters
import com.jeanloth.project.android.kotlin.feedme.features.command.presentation.products.ProductVM
import com.jeanloth.project.android.kotlin.feedme.features.dashboard.domain.CommandDetailIdArgument
import com.jeanloth.project.android.kotlin.feedme.features.dashboard.domain.FooterRoute
import com.jeanloth.project.android.kotlin.feedme.features.dashboard.domain.FooterRoute.Companion.fromVal
import com.jeanloth.project.android.kotlin.feedme.features.dashboard.presentation.HomePage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import splitties.toast.UnreliableToastApi
import java.io.File
import java.io.FileOutputStream


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

            val clients by clientVM.allSF.collectAsState()

            val basketItems by productVM.basketItems.collectAsState()

            val baskets by basketVM.baskets.collectAsState()

            val selectedClient by commandVM.client.collectAsState()
            val basketWrappers by commandVM.basketWrappers.collectAsState()
            val productWrappers by commandVM.productWrappers.collectAsState()
            val commandsByDate by commandVM.commandsByDate.collectAsState()

            val title = fromVal(navBackStackEntry?.destination?.route).title
            topBarState.value = fromVal(navBackStackEntry?.destination?.route).title != null
            bottomBarState.value = fromVal(navBackStackEntry?.destination?.route).displayFooter
            displayBackOrCloseState.value = fromVal(navBackStackEntry?.destination?.route).displayBackOrClose
            displayAddButtonState.value = fromVal(navBackStackEntry?.destination?.route).displayAddButton
            dialogType.value = fromVal(navBackStackEntry?.destination?.route).dialogType

            val keyboardController = LocalSoftwareKeyboardController.current
            val context = LocalContext.current

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
                                commandVM.clearCurrentCommand()
                                navController.navigate(FooterRoute.COMMAND_LIST.route)
                            }
                            else -> navController.popBackStack(FooterRoute.HOME.route, false)
                        }
                     },
                    onDialogDismiss = { keyboardController?.hide() },
                    addButtonActionType = dialogType.value,
                    content = {
                        NavHost(navController = navController, startDestination = FooterRoute.HOME.route, modifier = Modifier.padding(it)) {
                            // Home page
                            composable(FooterRoute.HOME.route) { HomePage(navController) }

                            // List of commands page
                            composable(FooterRoute.COMMAND_LIST.route) {
                                CommandListPage(
                                    commandsByDate,
                                    onClick = {
                                        navController.navigate(FooterRoute.buildCommandDetailRoute(it.toString()))
                                    }
                                )
                            }

                            // Detail of command
                            composable(FooterRoute.COMMAND_DETAIL.route, arguments = listOf(navArgument(CommandDetailIdArgument) { type = NavType.LongType })) { navBackStackEntry ->
                                CommandDetailPage(
                                    commandDetailVM = hiltViewModel()
                                )
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
                                        },
                                        onAddProduct = productVM::saveProduct,
                                        onUriEntered = { label, uri ->
                                            processToImageSaving(label, uri)
                                        }
                                    )
                                )
                            }

                            // Client list page
                            composable(FooterRoute.CLIENT.route) {
                                ClientListPage(
                                    viewModel = clientVM,
                                    onClientRemoved = clientVM::removeClient
                                )
                            }

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
                                    onAddProduct = productVM::saveProduct,
                                    onUriEntered = { label, imageUri ->
                                       processToImageSaving(label, imageUri)
                                    }
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
                                        },
                                        onAddProduct = productVM::saveProduct,
                                        onUriEntered = { label, uri ->
                                            processToImageSaving(label, uri)
                                        }
                                    )
                                )
                            }

                            // Add client page - Not in footer
                            composable(FooterRoute.ADD_CLIENT.route) {
                                AddClientPage(
                                    onValidateClick = {
                                        Toast.makeText(this@MainActivity, "Clic sur valider", Toast.LENGTH_SHORT)
                                    }
                                )
                            }
                        }
                    },
                    onNewClientAdded = clientVM::saveClient
                )
            }
        }
    }

    private fun processToImageSaving(label: String, imageUri: Uri) {
        val source = ImageDecoder.createSource(this.contentResolver,imageUri)
        Log.d("onUriEntered", "Image uri path : ${imageUri.path}")
        val bitmap = ImageDecoder.decodeBitmap(source)
        saveBitmapToInternalStorage(bitmap, label)
    }

    fun saveBitmapToInternalStorage(finalBitmap: Bitmap, productName : String): File? {
        val root: File = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return null

        val fname = "$productName.jpg"
        val file = File(root, fname)
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return file
    }
}


