package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FirebaseAuthDialog
import com.example.ui.components.OwnerLoginDialog
import com.example.ui.components.ProductDetailDialog
import com.example.ui.components.StoreTopBar
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.RepairScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ElectroFixApp()
            }
        }
    }
}

enum class CustomerDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME(Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    SHOP(Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "nav_shop"),
    REPAIR(Icons.Filled.Build, Icons.Outlined.Build, "nav_repair"),
    ORDERS(Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping, "nav_orders")
}

@Composable
fun ElectroFixApp(viewModel: ShopViewModel = viewModel()) {
    var currentDestination by remember { mutableStateOf(CustomerDestination.HOME) }
    var isCartOpen by remember { mutableStateOf(false) }
    var showOwnerLoginDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val strings by viewModel.appStrings.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val repairs by viewModel.allRepairs.collectAsStateWithLifecycle()
    val adminSettings by viewModel.adminSettings.collectAsStateWithLifecycle()
    val firebaseStatus by viewModel.firebaseStatus.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    // Customer / Owner Firebase Authentication Dialog
    if (showAuthDialog) {
        FirebaseAuthDialog(
            strings = strings,
            firebaseStatus = firebaseStatus,
            currentUser = currentUser,
            onDismiss = { showAuthDialog = false },
            onSignInEmail = { email, pass, onResult ->
                viewModel.firebaseSignIn(email, pass, onResult)
            },
            onSignUpEmail = { email, pass, onResult ->
                viewModel.firebaseSignUp(email, pass, onResult)
            },
            onSignInGuest = { onResult ->
                viewModel.firebaseSignInGuest(onResult)
            },
            onSignOut = {
                viewModel.firebaseSignOut()
            }
        )
    }

    // Owner Login Modal Dialog (Discreet merchant portal)
    if (showOwnerLoginDialog) {
        OwnerLoginDialog(
            strings = strings,
            adminSettings = adminSettings,
            onDismiss = { showOwnerLoginDialog = false },
            onUnlockSuccess = {
                showOwnerLoginDialog = false
                viewModel.setAdminMode(true)
            },
            onSavePassword = { newPassword ->
                viewModel.saveAdminPassword(newPassword)
            }
        )
    }

    // Product Detail Popup Dialog
    if (selectedProduct != null) {
        ProductDetailDialog(
            product = selectedProduct!!,
            onDismiss = { viewModel.selectProduct(null) },
            onAddToCart = { product -> viewModel.addToCart(product) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isCartOpen && !isAdminMode) {
                StoreTopBar(
                    currentLanguage = currentLanguage,
                    strings = strings,
                    cartItemCount = cartCount,
                    currentUser = currentUser,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onOpenCart = { isCartOpen = true },
                    onOpenAuth = { showAuthDialog = true },
                    onOpenOwnerLogin = { showOwnerLoginDialog = true }
                )
            }
        },
        bottomBar = {
            // Customer Navigation Bar (Regular customers NEVER see Admin, like Amazon)
            if (!isCartOpen && !isAdminMode) {
                NavigationBar(modifier = Modifier.testTag("main_navigation_bar")) {
                    CustomerDestination.values().forEach { dest ->
                        val isSelected = currentDestination == dest
                        val title = when (dest) {
                            CustomerDestination.HOME -> strings.navHome
                            CustomerDestination.SHOP -> strings.navShop
                            CustomerDestination.REPAIR -> strings.navRepair
                            CustomerDestination.ORDERS -> strings.navOrders
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentDestination = dest
                            },
                            icon = {
                                if (dest == CustomerDestination.ORDERS && orders.any { it.status != com.example.data.model.Order.STATUS_DELIVERED }) {
                                    BadgedBox(
                                        badge = {
                                            Badge {
                                                Text("${orders.count { it.status != com.example.data.model.Order.STATUS_DELIVERED }}")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = title
                                    )
                                }
                            },
                            label = { Text(title) },
                            modifier = Modifier.testTag(dest.tag)
                        )
                    }
                }
            } else if (isAdminMode) {
                // When Merchant Mode is active, show an Admin Navigation Bar with quick exit
                Surface(
                    color = BrandPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.adminModeBanner,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.setAdminMode(false) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.testTag("admin_bottom_exit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.exitAdminMode, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isAdminMode) {
                // Restricted Admin Screen for store owner only
                AdminScreen(
                    strings = strings,
                    isAdminUnlocked = true,
                    adminSettings = adminSettings,
                    onUnlockAdmin = { unlocked -> viewModel.setAdminMode(unlocked) },
                    products = allProducts,
                    orders = orders,
                    repairs = repairs,
                    firebaseStatus = firebaseStatus,
                    firebaseUser = currentUser,
                    onSyncToFirestore = { viewModel.syncAllToCloudFirestore() },
                    onPullFromFirestore = { viewModel.pullAllFromCloudFirestore() },
                    onFirebaseSignIn = { email, pass, cb -> viewModel.firebaseSignIn(email, pass, cb) },
                    onFirebaseSignUp = { email, pass, cb -> viewModel.firebaseSignUp(email, pass, cb) },
                    onFirebaseSignInGuest = { cb -> viewModel.firebaseSignInGuest(cb) },
                    onFirebaseSignOut = { viewModel.firebaseSignOut() },
                    onSaveAdminPassword = { newPassword ->
                        viewModel.saveAdminPassword(newPassword)
                    },
                    onResetDatabase = {
                        viewModel.resetStoreDatabase()
                    },
                    onUpdateProductPriceAndStock = { id, price, stock ->
                        viewModel.adminUpdatePriceAndStock(id, price, stock)
                    },
                    onSaveProduct = { product -> viewModel.adminSaveProduct(product) },
                    onDeleteProduct = { product -> viewModel.adminDeleteProduct(product) },
                    onUpdateOrderStatus = { orderId, status ->
                        viewModel.adminUpdateOrderStatus(orderId, status)
                    },
                    onUpdateRepairStatus = { repairId, status ->
                        viewModel.adminUpdateRepairStatus(repairId, status)
                    }
                )
            } else if (isCartOpen) {
                CartScreen(
                    strings = strings,
                    cartItems = cartItems,
                    subtotal = cartSubtotal,
                    onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                    onRemoveItem = { id -> viewModel.removeFromCart(id) },
                    onClearCart = { viewModel.clearCart() },
                    onBackToShop = { isCartOpen = false },
                    onPlaceOrder = { name, phone, address, payment, onSuccess ->
                        viewModel.placeOrder(name, phone, address, payment, onSuccess)
                    },
                    onOrderSuccess = {
                        isCartOpen = false
                        currentDestination = CustomerDestination.ORDERS
                    }
                )
            } else {
                when (currentDestination) {
                    CustomerDestination.HOME -> HomeScreen(
                        strings = strings,
                        products = allProducts,
                        orders = orders,
                        repairs = repairs,
                        onNavigateToShop = { currentDestination = CustomerDestination.SHOP },
                        onNavigateToRepair = { currentDestination = CustomerDestination.REPAIR },
                        onNavigateToOrders = { currentDestination = CustomerDestination.ORDERS },
                        onProductClick = { product -> viewModel.selectProduct(product) },
                        onAddToCart = { product -> viewModel.addToCart(product) },
                        onOpenOwnerLogin = { showOwnerLoginDialog = true }
                    )
                    CustomerDestination.SHOP -> ShopScreen(
                        strings = strings,
                        products = products,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        cartItemCount = cartCount,
                        onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                        onCategorySelect = { cat -> viewModel.setSelectedCategory(cat) },
                        onProductClick = { product -> viewModel.selectProduct(product) },
                        onAddToCart = { product -> viewModel.addToCart(product) },
                        onOpenCart = { isCartOpen = true }
                    )
                    CustomerDestination.REPAIR -> RepairScreen(
                        strings = strings,
                        repairs = repairs,
                        onSubmitRepair = { name, phone, type, title, desc, date, photo, onSuccess ->
                            viewModel.submitRepairRequest(name, phone, type, title, desc, date, photo, onSuccess)
                        }
                    )
                    CustomerDestination.ORDERS -> OrdersScreen(
                        strings = strings,
                        orders = orders,
                        onNavigateToShop = { currentDestination = CustomerDestination.SHOP }
                    )
                }
            }
        }
    }
}
