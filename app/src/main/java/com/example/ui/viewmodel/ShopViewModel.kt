package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.firebase.FirebaseService
import com.example.data.firebase.FirebaseStatus
import com.example.data.model.AdminSettings
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.RepairRequest
import com.example.data.repository.ShopRepository
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.AppStrings
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseService = FirebaseService(application)
    private val repository = ShopRepository(application, AppDatabase.getDatabase(application), firebaseService)

    val firebaseStatus: StateFlow<FirebaseStatus> = firebaseService.firebaseStatus
    val currentUser: StateFlow<FirebaseUser?> = firebaseService.currentUser

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.allCartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRepairs: StateFlow<List<RepairRequest>> = repository.allRepairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminSettings: StateFlow<AdminSettings?> = repository.adminSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _appStrings = MutableStateFlow(AppStrings(AppLanguage.EN))
    val appStrings: StateFlow<AppStrings> = _appStrings.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // Filtered products reactive flow
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.specifications.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartCount: StateFlow<Int> = cartItems.combine(cartItems) { items, _ ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Double> = cartItems.combine(cartItems) { items, _ ->
        items.sumOf { it.totalItemPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            try {
                repository.pullAllFromFirestore()
            } catch (_: Exception) {}
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        if (!product.isInStock) {
            _userFeedbackMessage.value = "Item is currently out of stock"
            return
        }
        viewModelScope.launch {
            repository.addToCart(product, quantity)
            _userFeedbackMessage.value = "Added \"${product.name}\" to cart"
        }
    }

    fun updateCartQuantity(productId: Long, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, newQuantity)
        }
    }

    fun removeFromCart(productId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
            _userFeedbackMessage.value = "Item removed from cart"
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        paymentMethod: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val currentCart = cartItems.value
            val result = repository.placeOrder(customerName, customerPhone, customerAddress, paymentMethod, currentCart)
            result.onSuccess { orderId ->
                _userFeedbackMessage.value = "Order $orderId placed successfully!"
                onSuccess(orderId)
            }.onFailure { error ->
                _userFeedbackMessage.value = "Failed to place order: ${error.message}"
            }
        }
    }

    fun submitRepairRequest(
        customerName: String,
        customerPhone: String,
        itemType: String,
        problemTitle: String,
        problemDescription: String,
        preferredDate: String,
        photoUri: String?,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.submitRepairRequest(
                customerName = customerName,
                customerPhone = customerPhone,
                itemType = itemType,
                problemTitle = problemTitle,
                problemDescription = problemDescription,
                preferredDate = preferredDate,
                photoUri = photoUri
            )
            result.onSuccess { repairId ->
                _userFeedbackMessage.value = "Repair request $repairId submitted!"
                onSuccess(repairId)
            }.onFailure { error ->
                _userFeedbackMessage.value = "Failed to submit repair: ${error.message}"
            }
        }
    }

    // Admin Functions
    fun adminUpdatePriceAndStock(productId: Long, price: Double, stock: Int) {
        viewModelScope.launch {
            repository.updateProductPriceAndStock(productId, price, stock)
            _userFeedbackMessage.value = "Product price and stock updated"
        }
    }

    fun adminSaveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.addProduct(product)
                _userFeedbackMessage.value = "Product added successfully"
            } else {
                repository.updateProduct(product)
                _userFeedbackMessage.value = "Product details updated"
            }
        }
    }

    fun adminDeleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _userFeedbackMessage.value = "Product removed from store"
        }
    }

    fun adminUpdateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            _userFeedbackMessage.value = "Order $orderId status updated to $status"
        }
    }

    fun adminUpdateRepairStatus(repairId: String, status: String) {
        viewModelScope.launch {
            repository.updateRepairStatus(repairId, status)
            _userFeedbackMessage.value = "Repair $repairId status updated to $status"
        }
    }

    fun saveAdminPassword(password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveAdminPassword(password)
            _userFeedbackMessage.value = if (password.isNotBlank()) "Admin password updated in database" else "Admin password cleared"
            onSuccess()
        }
    }

    fun resetStoreDatabase(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetDatabaseToInitial()
            _userFeedbackMessage.value = "Database sample catalog refreshed"
            onSuccess()
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        _appStrings.value = AppStrings(language)
    }

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == AppLanguage.EN) AppLanguage.GU else AppLanguage.EN
        setLanguage(next)
    }

    // ==========================================
    // Firebase Cloud Sync & Authentication Actions
    // ==========================================

    fun syncAllToCloudFirestore(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.syncAllToFirestore()
            result.onSuccess { msg ->
                _userFeedbackMessage.value = msg
                onComplete(true, msg)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Sync failed"
                _userFeedbackMessage.value = errorMsg
                onComplete(false, errorMsg)
            }
        }
    }

    fun pullAllFromCloudFirestore(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.pullAllFromFirestore()
            result.onSuccess { msg ->
                _userFeedbackMessage.value = msg
                onComplete(true, msg)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Pull failed"
                _userFeedbackMessage.value = errorMsg
                onComplete(false, errorMsg)
            }
        }
    }

    fun firebaseSignIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.firebaseSignIn(email, pass)
            res.onSuccess { user ->
                _userFeedbackMessage.value = "Welcome ${user.email ?: "User"}!"
                onResult(true, null)
            }.onFailure { err ->
                val msg = err.localizedMessage ?: "Authentication failed"
                _userFeedbackMessage.value = msg
                onResult(false, msg)
            }
        }
    }

    fun firebaseSignUp(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.firebaseSignUp(email, pass)
            res.onSuccess { user ->
                _userFeedbackMessage.value = "Account created: ${user.email}"
                onResult(true, null)
            }.onFailure { err ->
                val msg = err.localizedMessage ?: "Sign up failed"
                _userFeedbackMessage.value = msg
                onResult(false, msg)
            }
        }
    }

    fun firebaseSignInGuest(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.firebaseSignInGuest()
            res.onSuccess {
                _userFeedbackMessage.value = "Signed in as Guest"
                onResult(true, null)
            }.onFailure { err ->
                val msg = err.localizedMessage ?: "Guest login failed"
                _userFeedbackMessage.value = msg
                onResult(false, msg)
            }
        }
    }

    fun firebaseSignOut() {
        repository.firebaseSignOut()
        _userFeedbackMessage.value = "Signed out of Firebase"
    }
}
