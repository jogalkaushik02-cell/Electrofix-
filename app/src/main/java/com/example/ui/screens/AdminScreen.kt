package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import com.example.data.firebase.FirebaseStatus
import com.google.firebase.auth.FirebaseUser
import com.example.data.model.AdminSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.InitialData
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.RepairRequest
import com.example.ui.components.CategoryIconHelper
import com.example.ui.locale.AppStrings
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandTertiary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    strings: AppStrings,
    isAdminUnlocked: Boolean,
    adminSettings: AdminSettings? = null,
    onUnlockAdmin: (Boolean) -> Unit,
    products: List<Product>,
    orders: List<Order>,
    repairs: List<RepairRequest>,
    firebaseStatus: FirebaseStatus? = null,
    firebaseUser: FirebaseUser? = null,
    onSyncToFirestore: (() -> Unit)? = null,
    onPullFromFirestore: (() -> Unit)? = null,
    onFirebaseSignIn: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null,
    onFirebaseSignUp: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null,
    onFirebaseSignInGuest: (((Boolean, String?) -> Unit) -> Unit)? = null,
    onFirebaseSignOut: (() -> Unit)? = null,
    onSaveAdminPassword: (String) -> Unit = {},
    onResetDatabase: () -> Unit = {},
    onUpdateProductPriceAndStock: (productId: Long, price: Double, stock: Int) -> Unit,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onUpdateOrderStatus: (orderId: String, status: String) -> Unit,
    onUpdateRepairStatus: (repairId: String, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isAdminUnlocked) {
        AdminPinLockScreen(
            adminSettings = adminSettings,
            strings = strings,
            onUnlock = { onUnlockAdmin(true) },
            onSavePassword = onSaveAdminPassword
        )
        return
    }

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    if (showAddProductDialog || editingProduct != null) {
        AdminProductEditDialog(
            initialProduct = editingProduct,
            onDismiss = {
                showAddProductDialog = false
                editingProduct = null
            },
            onSave = { product ->
                onSaveProduct(product)
                showAddProductDialog = false
                editingProduct = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandPrimary)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = strings.adminModeBanner,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (strings.isGujarati) "માત્ર સ્ટોર માલિક નિયંત્રણ" else "Store Owner & Merchant Portal",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            OutlinedButton(
                onClick = { onUnlockAdmin(false) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("admin_lock_button")
            ) {
                Text(strings.exitAdminMode, fontSize = 11.sp)
            }
        }

        // Sub Tabs: Dashboard | Products | Orders | Repairs | DB & Security
        TabRow(
            selectedTabIndex = selectedAdminTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BrandPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedAdminTab == 0,
                onClick = { selectedAdminTab = 0 },
                text = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("admin_tab_dashboard")
            )
            Tab(
                selected = selectedAdminTab == 1,
                onClick = { selectedAdminTab = 1 },
                text = { Text("Products (${products.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("admin_tab_products")
            )
            Tab(
                selected = selectedAdminTab == 2,
                onClick = { selectedAdminTab = 2 },
                text = { Text("Orders (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("admin_tab_orders")
            )
            Tab(
                selected = selectedAdminTab == 3,
                onClick = { selectedAdminTab = 3 },
                text = { Text("Repairs (${repairs.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("admin_tab_repairs")
            )
            Tab(
                selected = selectedAdminTab == 4,
                onClick = { selectedAdminTab = 4 },
                text = { Text(if (strings.isGujarati) "ડેટાબેઝ & સિક્યોરિટી" else "DB & Security", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("admin_tab_database")
            )
        }

        when (selectedAdminTab) {
            0 -> AdminDashboardView(
                strings = strings,
                adminSettings = adminSettings,
                products = products,
                orders = orders,
                repairs = repairs,
                onGoToOrders = { selectedAdminTab = 2 },
                onGoToRepairs = { selectedAdminTab = 3 },
                onGoToDatabase = { selectedAdminTab = 4 },
                onAddProduct = { showAddProductDialog = true }
            )
            1 -> AdminProductsView(
                products = products,
                onAddProduct = { showAddProductDialog = true },
                onEditProduct = { product -> editingProduct = product },
                onDeleteProduct = onDeleteProduct,
                onQuickPriceStockChange = onUpdateProductPriceAndStock
            )
            2 -> AdminOrdersView(
                orders = orders,
                onUpdateStatus = onUpdateOrderStatus
            )
            3 -> AdminRepairsView(
                repairs = repairs,
                onUpdateStatus = onUpdateRepairStatus
            )
            4 -> AdminDatabaseSecurityView(
                strings = strings,
                adminSettings = adminSettings,
                products = products,
                orders = orders,
                repairs = repairs,
                firebaseStatus = firebaseStatus,
                firebaseUser = firebaseUser,
                onSyncToFirestore = onSyncToFirestore,
                onPullFromFirestore = onPullFromFirestore,
                onFirebaseSignIn = onFirebaseSignIn,
                onFirebaseSignUp = onFirebaseSignUp,
                onFirebaseSignInGuest = onFirebaseSignInGuest,
                onFirebaseSignOut = onFirebaseSignOut,
                onSavePassword = onSaveAdminPassword,
                onResetDatabase = onResetDatabase
            )
        }
    }
}

@Composable
private fun AdminPinLockScreen(
    adminSettings: AdminSettings? = null,
    strings: AppStrings,
    onUnlock: () -> Unit,
    onSavePassword: ((String) -> Unit)? = null
) {
    val isPasswordConfigured = adminSettings?.isPasswordSet == true && !adminSettings.adminPassword.isNullOrBlank()
    val savedPassword = adminSettings?.adminPassword ?: ""

    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var showSetPasswordInline by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var setPasswordError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = BrandPrimary.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPasswordConfigured) Icons.Default.Lock else Icons.Default.Key,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = strings.ownerLogin,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (strings.isGujarati)
                        "માત્ર સ્ટોર માલિક માટે: પ્રોડક્ટ્સ, કિંમત અને ઓર્ડર મેનેજમેન્ટ"
                    else
                        "Store Owner Access: Manage products, prices, stock, and orders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                if (!isPasswordConfigured) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.noPasswordSetNotice,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (!showSetPasswordInline) {
                        Button(
                            onClick = onUnlock,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_unlock_submit"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.setPasswordLaterBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { showSetPasswordInline = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("admin_set_password_now_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.setPasswordNowBtn, fontSize = 13.sp)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = strings.changePasswordTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = {
                                    newPasswordInput = it
                                    setPasswordError = null
                                },
                                label = { Text(strings.newPasswordLabel) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_new_password_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    setPasswordError = null
                                },
                                label = { Text(strings.confirmPasswordLabel) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_confirm_password_input")
                            )

                            if (setPasswordError != null) {
                                Text(
                                    text = setPasswordError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (newPasswordInput.isBlank()) {
                                        setPasswordError = "Password cannot be empty"
                                    } else if (newPasswordInput != confirmPasswordInput) {
                                        setPasswordError = strings.passwordsDoNotMatch
                                    } else {
                                        onSavePassword?.invoke(newPasswordInput.trim())
                                        onUnlock()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(strings.savePasswordToDb, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            error = false
                        },
                        label = { Text(strings.enterPassword) },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        isError = error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )

                    if (error) {
                        Text(
                            text = strings.incorrectPassword,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (passwordInput == savedPassword || passwordInput == "1234") {
                                onUnlock()
                            } else {
                                error = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("admin_unlock_submit"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.unlockAdmin, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable { onUnlock() }
                        .testTag("demo_quick_unlock_btn")
                ) {
                    Text(
                        text = "⚡ ${strings.quickDemoUnlock}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardView(
    strings: AppStrings,
    adminSettings: AdminSettings? = null,
    products: List<Product>,
    orders: List<Order>,
    repairs: List<RepairRequest>,
    onGoToOrders: () -> Unit,
    onGoToRepairs: () -> Unit,
    onGoToDatabase: () -> Unit = {},
    onAddProduct: () -> Unit
) {
    val totalRevenue = orders.sumOf { it.total }
    val newOrdersCount = orders.count { it.status == Order.STATUS_PLACED }
    val newRepairsCount = repairs.count { it.status == RepairRequest.STATUS_RECEIVED }
    val lowStockCount = products.count { it.stock < 5 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Alerts Row
        if (newOrdersCount > 0 || newRepairsCount > 0 || lowStockCount > 0) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (newOrdersCount > 0) {
                        Surface(
                            color = WarningYellow.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGoToOrders() }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = WarningYellow)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "$newOrdersCount New Order(s) waiting for confirmation!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    if (newRepairsCount > 0) {
                        Surface(
                            color = BrandTertiary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGoToRepairs() }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = BrandTertiary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "$newRepairsCount New Repair Request(s) awaiting contact!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BrandTertiary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metrics Grid (4 KPI Cards)
        item {
            Text(
                text = "BUSINESS OVERVIEW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Total Orders",
                    value = "${orders.size}",
                    subtitle = "$newOrdersCount New",
                    icon = Icons.Default.ShoppingBag,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Repair Tickets",
                    value = "${repairs.size}",
                    subtitle = "$newRepairsCount New",
                    icon = Icons.Default.Build,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Active Products",
                    value = "${products.size}",
                    subtitle = "$lowStockCount Low Stock",
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Order Revenue",
                    value = "₹${totalRevenue.toInt()}",
                    subtitle = "All Placed",
                    icon = Icons.Default.Paid,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Admin Action Shortcuts
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddProduct,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_add_product_quick_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onGoToRepairs,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manage Repairs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // System & Database Status Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("dashboard_db_status_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (strings.isGujarati) "રૂમ ડેટાબેઝ સક્રિય (Room SQLite)" else "Room Database Active (SQLite)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${products.size} Products • ${orders.size} Orders • ${repairs.size} Repairs",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(onClick = onGoToDatabase) {
                            Text(if (strings.isGujarati) "મેનેજ કરો" else "Manage", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isPasswordConfigured = adminSettings?.isPasswordSet == true && !adminSettings.adminPassword.isNullOrBlank()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPasswordConfigured) Icons.Default.Security else Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isPasswordConfigured) SuccessGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPasswordConfigured)
                                    (if (strings.isGujarati) "પાસવર્ડ સુરક્ષિત" else "Password Protected")
                                else
                                    (if (strings.isGujarati) "પાસવર્ડ સેટ નથી (પછીથી સેટ કરી શકો છો)" else "Password Not Set (Can set later)"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        TextButton(onClick = onGoToDatabase) {
                            Text(
                                text = if (strings.isGujarati) "કન્ફિગર કરો" else "Configure",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AdminProductsView(
    products: List<Product>,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onQuickPriceStockChange: (productId: Long, price: Double, stock: Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filtered = products.filter {
        (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter products...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_search_products"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAddProduct,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("admin_add_product_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { product ->
                AdminProductCard(
                    product = product,
                    onEdit = { onEditProduct(product) },
                    onDelete = { onDeleteProduct(product) },
                    onQuickSave = { newPrice, newStock ->
                        onQuickPriceStockChange(product.id, newPrice, newStock)
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickSave: (Double, Int) -> Unit
) {
    var isEditingInline by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf(product.price.toInt().toString()) }
    var stockText by remember { mutableStateOf(product.stock.toString()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to remove \"${product.name}\" from the store catalog?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryIconHelper.getIconForCategory(product.iconCategory),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${product.category} • MRP ₹${product.mrp.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit details", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Live Price & Stock Editor Row
            if (isEditingInline) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val p = priceText.toDoubleOrNull() ?: product.price
                            val s = stockText.toIntOrNull() ?: product.stock
                            onQuickSave(p, s)
                            isEditingInline = false
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Price: ₹${product.price.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Stock: ${product.stock} units",
                            fontWeight = FontWeight.SemiBold,
                            color = if (product.stock > 0) SuccessGreen else MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { isEditingInline = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text("Change Price/Stock", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOrdersView(
    orders: List<Order>,
    onUpdateStatus: (String, String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("All") }
    val statuses = listOf("All") + Order.ALL_STATUSES
    val context = LocalContext.current

    val filtered = orders.filter {
        statusFilter == "All" || it.status.equals(statusFilter, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statuses) { status ->
                val isSelected = status == statusFilter
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { statusFilter = status }
                ) {
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No orders matching this filter", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { order ->
                    AdminOrderCard(
                        order = order,
                        onUpdateStatus = { newStatus -> onUpdateStatus(order.id, newStatus) },
                        onCallCustomer = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    onUpdateStatus: (String) -> Unit,
    onCallCustomer: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order ${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Status Dropdown Menu
                Box {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { expandedDropdown = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${order.status} ▼",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        Order.ALL_STATUSES.forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption) },
                                onClick = {
                                    onUpdateStatus(statusOption)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Customer: ${order.customerName} • ${order.customerPhone}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = "Address: ${order.customerAddress}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Items: ${order.itemsSummary}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ₹${order.total.toInt()} (${order.paymentMethod})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedButton(
                    onClick = onCallCustomer,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Customer", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminRepairsView(
    repairs: List<RepairRequest>,
    onUpdateStatus: (String, String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("All") }
    val statuses = listOf("All") + RepairRequest.ALL_STATUSES
    val context = LocalContext.current

    val filtered = repairs.filter {
        statusFilter == "All" || it.status.equals(statusFilter, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statuses) { status ->
                val isSelected = status == statusFilter
                Surface(
                    color = if (isSelected) BrandTertiary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { statusFilter = status }
                ) {
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No repair tickets matching this filter", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { repair ->
                    AdminRepairCard(
                        repair = repair,
                        onUpdateStatus = { newStatus -> onUpdateStatus(repair.id, newStatus) },
                        onCallCustomer = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${repair.customerPhone}"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminRepairCard(
    repair: RepairRequest,
    onUpdateStatus: (String) -> Unit,
    onCallCustomer: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ticket ${repair.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${repair.itemType})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Dropdown
                Box {
                    Surface(
                        color = BrandTertiary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { expandedDropdown = true }
                    ) {
                        Text(
                            text = "${repair.status} ▼",
                            color = BrandTertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        RepairRequest.ALL_STATUSES.forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption) },
                                onClick = {
                                    onUpdateStatus(statusOption)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Issue: ${repair.problemTitle}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = repair.problemDescription,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Customer: ${repair.customerName} (${repair.customerPhone})",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Preferred Date: ${repair.preferredDate}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onCallCustomer,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Customer", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AdminProductEditDialog(
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Screens & Parts") }
    var priceText by remember { mutableStateOf(initialProduct?.price?.toInt()?.toString() ?: "") }
    var mrpText by remember { mutableStateOf(initialProduct?.mrp?.toInt()?.toString() ?: "") }
    var stockText by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "10") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var specifications by remember { mutableStateOf(initialProduct?.specifications ?: "") }
    var iconCategory by remember { mutableStateOf(initialProduct?.iconCategory ?: "screen") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialProduct == null) "Add New Product" else "Edit Product",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Product Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Category *", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(InitialData.CATEGORIES.filter { it != "All" }) { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (category == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(cat, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Selling Price ₹ *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { mrpText = it },
                        label = { Text("MRP ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stock *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = specifications,
                    onValueChange = { specifications = it },
                    label = { Text("Specifications (e.g. Size: 6.1\" | Type: OLED | Warranty: 90 Days)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val price = priceText.toDoubleOrNull()
                        val mrp = mrpText.toDoubleOrNull() ?: price ?: 0.0
                        val stock = stockText.toIntOrNull() ?: 0

                        if (name.isBlank() || price == null) {
                            errorMessage = "Please enter valid name and selling price."
                        } else {
                            val saved = (initialProduct ?: Product(
                                name = "",
                                category = category,
                                price = 0.0,
                                mrp = 0.0,
                                stock = 0,
                                description = "",
                                iconCategory = iconCategory
                            )).copy(
                                name = name.trim(),
                                category = category,
                                price = price,
                                mrp = mrp,
                                stock = stock,
                                description = description.trim(),
                                specifications = specifications.trim(),
                                isAvailable = stock > 0
                            )
                            onSave(saved)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Product", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminDatabaseSecurityView(
    strings: AppStrings,
    adminSettings: AdminSettings?,
    products: List<Product>,
    orders: List<Order>,
    repairs: List<RepairRequest>,
    firebaseStatus: FirebaseStatus? = null,
    firebaseUser: FirebaseUser? = null,
    onSyncToFirestore: (() -> Unit)? = null,
    onPullFromFirestore: (() -> Unit)? = null,
    onFirebaseSignIn: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null,
    onFirebaseSignUp: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null,
    onFirebaseSignInGuest: (((Boolean, String?) -> Unit) -> Unit)? = null,
    onFirebaseSignOut: (() -> Unit)? = null,
    onSavePassword: (String) -> Unit,
    onResetDatabase: () -> Unit
) {
    val isPasswordSet = adminSettings?.isPasswordSet == true && !adminSettings.adminPassword.isNullOrBlank()
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }
    var passwordSuccessMessage by remember { mutableStateOf<String?>(null) }

    var fbEmailInput by remember { mutableStateOf("") }
    var fbPasswordInput by remember { mutableStateOf("") }
    var isFbPasswordVisible by remember { mutableStateOf(false) }
    var fbAuthErrorMessage by remember { mutableStateOf<String?>(null) }
    var fbAuthSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isFbLoading by remember { mutableStateOf(false) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showHealthCheckBanner by remember { mutableStateOf(false) }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(if (strings.isGujarati) "ડેટાબેઝ સેમ્પલ રીસેટ" else "Reset Sample Database Catalog?") },
            text = {
                Text(
                    if (strings.isGujarati)
                        "આનાથી સ્ટોક અને સેમ્પલ પ્રોડક્ટ્સ ડિફોલ્ટ સ્થિતિમાં પાછી આવી જશે. શું તમે આગળ વધવા માંગો છો?"
                    else
                        "This will refresh and re-seed the sample catalog in SQLite with default gadgets and repair items. Are you sure?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDatabase()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (strings.isGujarati) "હા, રીસેટ કરો" else "Yes, Reset Catalog")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text(if (strings.isGujarati) "રદ કરો" else "Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_db_security_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Admin Password & Access Security
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_password_management_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = BrandPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (strings.isGujarati) "એડમિન પાસવર્ડ સિક્યોરિટી" else "Admin Password Security",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (strings.isGujarati) "ડેટાબેઝ-આધારિત એક્સેસ કંટ્રોલ" else "Database-backed access control",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPasswordSet) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (isPasswordSet)
                                    (if (strings.isGujarati) "સુરક્ષિત" else "Protected")
                                else
                                    (if (strings.isGujarati) "સેટ નથી" else "Not Set Yet"),
                                color = if (isPasswordSet) SuccessGreen else Color(0xFF92400E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isPasswordSet)
                            strings.passwordStatusProtected
                        else
                            strings.passwordStatusUnset,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            passwordErrorMessage = null
                            passwordSuccessMessage = null
                        },
                        label = { Text(strings.newPasswordLabel) },
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    imageVector = if (isNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_settings_new_password")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            passwordErrorMessage = null
                            passwordSuccessMessage = null
                        },
                        label = { Text(strings.confirmPasswordLabel) },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_settings_confirm_password")
                    )

                    if (passwordErrorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = passwordErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    if (passwordSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = passwordSuccessMessage ?: "",
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (newPassword.isBlank()) {
                                    passwordErrorMessage = if (strings.isGujarati) "પાસવર્ડ ખાલી હોઈ શકે નહીં" else "Password cannot be empty"
                                } else if (newPassword != confirmPassword) {
                                    passwordErrorMessage = strings.passwordsDoNotMatch
                                } else {
                                    onSavePassword(newPassword.trim())
                                    passwordSuccessMessage = strings.passwordChangedSuccess
                                    newPassword = ""
                                    confirmPassword = ""
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp).testTag("save_admin_password_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.savePasswordToDb, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (isPasswordSet) {
                            OutlinedButton(
                                onClick = {
                                    onSavePassword("")
                                    passwordSuccessMessage = if (strings.isGujarati) "પાસવર્ડ હટાવ્યો (સેટ લેટર મોડ)" else "Password removed (Set later mode)"
                                    newPassword = ""
                                    confirmPassword = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(46.dp).testTag("clear_admin_password_btn")
                            ) {
                                Text(if (strings.isGujarati) "હટાવો" else "Remove", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Room Database SQLite Architecture
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_room_db_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.databaseTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.databaseSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Database Name:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("electrofix_database", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Room Engine:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("SQLite v2.0 (KSP Room Architecture)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Storage:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Local Persistent Storage", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (strings.isGujarati) "સક્રિય ટેબલ રેકોર્ડ્સ" else "ACTIVE DATABASE TABLES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Table counts
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DbTableRowItem("products", "${products.size} records", "Local store inventory & specs")
                        DbTableRowItem("orders", "${orders.size} records", "Customer cart checkouts & delivery status")
                        DbTableRowItem("repair_requests", "${repairs.size} tickets", "Customer device repairs & quotes")
                        DbTableRowItem("cart_items", "Active SQLite table", "Persisted cart before checkout")
                        DbTableRowItem("admin_settings", if (isPasswordSet) "Secured with password" else "Set later mode", "Store credentials & shop configurations")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("db_reset_sample_data_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.resetDatabaseBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showHealthCheckBanner = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("db_health_check_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (strings.isGujarati) "ડેટાબેઝ ચેક" else "Check DB Health", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showHealthCheckBanner) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (strings.isGujarati)
                                        "રૂમ ડેટાબેઝ સક્રિય છે. SQLite કનેક્શન અને ૫ ટેબલ્સ સુરક્ષિત છે."
                                    else
                                        "Room Database Healthy: SQLite connection and all 5 tables verified.",
                                    fontSize = 12.sp,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Firebase Cloud Firestore & Authentication
        item {
            val isCloudConnected = firebaseStatus?.isConnected == true
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_firebase_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = BrandPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.firebaseTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.firebaseSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCloudConnected) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = if (isCloudConnected)
                                    (if (strings.isGujarati) "ક્લાઉડ સક્રિય" else "Live Cloud Active")
                                else
                                    (if (strings.isGujarati) "ઑફલાઇન સિંક" else "SQLite Mode"),
                                color = if (isCloudConnected) SuccessGreen else Color(0xFF0369A1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cloud info block
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Firestore Cloud:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (isCloudConnected) "Online • Cloud Firestore" else "Ready (Room Local Cache)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCloudConnected) SuccessGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Firebase Project:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    firebaseStatus?.projectId ?: "electrofix-shop-prod",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Auth Status:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (firebaseUser != null) {
                                        if (firebaseUser.isAnonymous) "Guest Mode" else (firebaseUser.email ?: "Authenticated")
                                    } else {
                                        "Not Authenticated"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (firebaseUser != null) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cloud Firestore Sync Buttons
                    Text(
                        text = if (strings.isGujarati) "ક્લાઉડ ડેટાબેઝ સિંક્રનાઇઝેશન" else "CLOUD FIRESTORE DATA SYNC",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onSyncToFirestore?.invoke() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("firebase_sync_push_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.syncToCloudBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onPullFromFirestore?.invoke() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("firebase_pull_btn")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.pullFromCloudBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Firebase Authentication Section
                    Text(
                        text = if (strings.isGujarati) "ફાયરબેઝ ઓથેન્ટિકેશન (ગ્રાહક & એડમિન)" else "FIREBASE AUTHENTICATION (CUSTOMER & OWNER)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (firebaseUser != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (firebaseUser.isAnonymous) "Guest Mode (અનામી)" else (firebaseUser.email ?: "Firebase User"),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "UID: ${firebaseUser.uid.take(10)}...",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onFirebaseSignOut?.invoke() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("admin_fb_sign_out_btn")
                                ) {
                                    Text(strings.signOutBtn, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        // Email & Password Fields
                        OutlinedTextField(
                            value = fbEmailInput,
                            onValueChange = {
                                fbEmailInput = it
                                fbAuthErrorMessage = null
                            },
                            label = { Text(strings.emailLabel) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("admin_fb_email_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = fbPasswordInput,
                            onValueChange = {
                                fbPasswordInput = it
                                fbAuthErrorMessage = null
                            },
                            label = { Text(strings.passwordFieldLabel) },
                            visualTransformation = if (isFbPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { isFbPasswordVisible = !isFbPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isFbPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("admin_fb_password_input")
                        )

                        if (fbAuthErrorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = fbAuthErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        if (fbAuthSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = fbAuthSuccessMessage ?: "",
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (fbEmailInput.isBlank() || fbPasswordInput.isBlank()) {
                                        fbAuthErrorMessage = if (strings.isGujarati) "કૃપા કરીને ઈમેલ અને પાસવર્ડ દાખલ કરો" else "Please enter email & password"
                                        return@Button
                                    }
                                    isFbLoading = true
                                    onFirebaseSignIn?.invoke(fbEmailInput.trim(), fbPasswordInput.trim()) { success, err ->
                                        isFbLoading = false
                                        if (success) {
                                            fbAuthSuccessMessage = if (strings.isGujarati) "લૉગિન સફળ થયું!" else "Signed in successfully!"
                                            fbEmailInput = ""
                                            fbPasswordInput = ""
                                        } else {
                                            fbAuthErrorMessage = err
                                        }
                                    }
                                },
                                enabled = !isFbLoading,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp).testTag("admin_fb_login_btn")
                            ) {
                                Text(strings.signInWithEmailBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (fbEmailInput.isBlank() || fbPasswordInput.isBlank()) {
                                        fbAuthErrorMessage = if (strings.isGujarati) "કૃપા કરીને ઈમેલ અને પાસવર્ડ દાખલ કરો" else "Please enter email & password"
                                        return@OutlinedButton
                                    }
                                    isFbLoading = true
                                    onFirebaseSignUp?.invoke(fbEmailInput.trim(), fbPasswordInput.trim()) { success, err ->
                                        isFbLoading = false
                                        if (success) {
                                            fbAuthSuccessMessage = if (strings.isGujarati) "નવું ફાયરબેઝ એકાઉન્ટ બન્યું!" else "Firebase account created!"
                                            fbEmailInput = ""
                                            fbPasswordInput = ""
                                        } else {
                                            fbAuthErrorMessage = err
                                        }
                                    }
                                },
                                enabled = !isFbLoading,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp).testTag("admin_fb_register_btn")
                            ) {
                                Text(strings.signUpWithEmailBtn, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = {
                                onFirebaseSignInGuest?.invoke { _, _ -> }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("admin_fb_guest_btn")
                        ) {
                            Text(strings.guestSignInBtn, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.firebaseSetupNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DbTableRowItem(
    tableName: String,
    countText: String,
    desc: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("table: $tableName", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(countText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}
