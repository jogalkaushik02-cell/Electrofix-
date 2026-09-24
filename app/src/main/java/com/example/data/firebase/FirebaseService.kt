package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.RepairRequest
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class FirebaseStatus(
    val isConnected: Boolean,
    val projectId: String?,
    val currentUserEmail: String?,
    val currentUserId: String?,
    val isAnonymous: Boolean,
    val statusMessage: String
)

class FirebaseService(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseService"
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_ORDERS = "orders"
        private const val COLLECTION_REPAIRS = "repair_requests"
    }

    private var _isInitialized: Boolean = false
    val isInitialized: Boolean get() = _isInitialized

    val auth: FirebaseAuth?
    val firestore: FirebaseFirestore?

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _firebaseStatus = MutableStateFlow(
        FirebaseStatus(
            isConnected = false,
            projectId = null,
            currentUserEmail = null,
            currentUserId = null,
            isAnonymous = false,
            statusMessage = "Initializing Firebase..."
        )
    )
    val firebaseStatus: StateFlow<FirebaseStatus> = _firebaseStatus.asStateFlow()

    init {
        var initializedApp: FirebaseApp? = null
        try {
            val apps = FirebaseApp.getApps(context)
            initializedApp = if (apps.isNotEmpty()) {
                FirebaseApp.getInstance()
            } else {
                FirebaseApp.initializeApp(context)
            }
            _isInitialized = initializedApp != null
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp initialization failed (waiting for google-services.json): ${e.message}")
            _isInitialized = false
        }

        if (_isInitialized && initializedApp != null) {
            auth = try {
                val a = FirebaseAuth.getInstance()
                a.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    _currentUser.value = user
                    updateStatus(
                        isConnected = true,
                        projectId = initializedApp.options.projectId,
                        currentUserEmail = user?.email,
                        currentUserId = user?.uid,
                        isAnonymous = user?.isAnonymous == true,
                        message = if (user != null) {
                            if (user.isAnonymous) "Signed in as Guest (${user.uid.take(6)}...)"
                            else "Authenticated as ${user.email}"
                        } else {
                            "Firebase Connected (Project: ${initializedApp.options.projectId ?: "default"})"
                        }
                    )
                }
                a
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseAuth not available: ${e.message}")
                null
            }

            firestore = try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseFirestore not available: ${e.message}")
                null
            }

            val user = auth?.currentUser
            updateStatus(
                isConnected = true,
                projectId = initializedApp.options.projectId,
                currentUserEmail = user?.email,
                currentUserId = user?.uid,
                isAnonymous = user?.isAnonymous == true,
                message = "Firebase Connected: Project '${initializedApp.options.projectId ?: "default"}'"
            )
        } else {
            auth = null
            firestore = null
            updateStatus(
                isConnected = false,
                projectId = null,
                currentUserEmail = null,
                currentUserId = null,
                isAnonymous = false,
                message = "Local SQLite Mode Active. Add google-services.json to /app to enable Live Firebase Cloud Sync."
            )
        }
    }

    private fun updateStatus(
        isConnected: Boolean,
        projectId: String?,
        currentUserEmail: String?,
        currentUserId: String?,
        isAnonymous: Boolean,
        message: String
    ) {
        _firebaseStatus.value = FirebaseStatus(
            isConnected = isConnected,
            projectId = projectId,
            currentUserEmail = currentUserEmail,
            currentUserId = currentUserId,
            isAnonymous = isAnonymous,
            statusMessage = message
        )
    }

    // ==========================================
    // Firebase Authentication
    // ==========================================

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized (add google-services.json to /app)"))
        try {
            val result = a.signInWithEmailAndPassword(email.trim(), pass).awaitResult()
            val user = result.user ?: throw IllegalStateException("User is null after login")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized (add google-services.json to /app)"))
        try {
            val result = a.createUserWithEmailAndPassword(email.trim(), pass).awaitResult()
            val user = result.user ?: throw IllegalStateException("User is null after signup")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
        try {
            val result = a.signInAnonymously().awaitResult()
            val user = result.user ?: throw IllegalStateException("Anonymous user is null")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
        _currentUser.value = null
        val current = _firebaseStatus.value
        _firebaseStatus.value = current.copy(currentUserEmail = null, currentUserId = null, isAnonymous = false)
    }

    // ==========================================
    // Cloud Firestore Sync: Products
    // ==========================================

    suspend fun uploadProducts(products: List<Product>): Result<Int> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        try {
            var count = 0
            for (product in products) {
                val doc = mapOf(
                    "id" to product.id,
                    "name" to product.name,
                    "category" to product.category,
                    "price" to product.price,
                    "mrp" to product.mrp,
                    "stock" to product.stock,
                    "description" to product.description,
                    "specifications" to product.specifications,
                    "isAvailable" to product.isAvailable,
                    "iconCategory" to product.iconCategory,
                    "imageUrl" to product.imageUrl
                )
                db.collection(COLLECTION_PRODUCTS)
                    .document(product.id.toString())
                    .set(doc, SetOptions.merge())
                    .awaitResult()
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        try {
            val snapshot = db.collection(COLLECTION_PRODUCTS).get().awaitResult()
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                    val name = doc.getString("name") ?: ""
                    val category = doc.getString("category") ?: "Electronics"
                    val price = doc.getDouble("price") ?: 0.0
                    val mrp = doc.getDouble("mrp") ?: price
                    val stock = doc.getLong("stock")?.toInt() ?: 0
                    val description = doc.getString("description") ?: ""
                    val specifications = doc.getString("specifications") ?: ""
                    val isAvailable = doc.getBoolean("isAvailable") ?: (stock > 0)
                    val iconCategory = doc.getString("iconCategory") ?: "phone"
                    val imageUrl = doc.getString("imageUrl") ?: ""

                    Product(
                        id = id,
                        name = name,
                        category = category,
                        price = price,
                        mrp = mrp,
                        stock = stock,
                        description = description,
                        specifications = specifications,
                        isAvailable = isAvailable,
                        iconCategory = iconCategory,
                        imageUrl = imageUrl
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // Cloud Firestore Sync: Orders
    // ==========================================

    suspend fun uploadOrder(order: Order): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        try {
            val doc = mapOf(
                "id" to order.id,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "customerAddress" to order.customerAddress,
                "paymentMethod" to order.paymentMethod,
                "itemsSummary" to order.itemsSummary,
                "subtotal" to order.subtotal,
                "deliveryFee" to order.deliveryFee,
                "total" to order.total,
                "status" to order.status,
                "createdAt" to order.createdAt
            )
            val docId = order.id.replace("#", "order_")
            db.collection(COLLECTION_ORDERS)
                .document(docId)
                .set(doc, SetOptions.merge())
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadOrders(orders: List<Order>): Result<Int> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        try {
            var count = 0
            for (order in orders) {
                uploadOrder(order)
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        try {
            val snapshot = db.collection(COLLECTION_ORDERS).get().awaitResult()
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: ("#" + doc.id.removePrefix("order_"))
                    val customerName = doc.getString("customerName") ?: ""
                    val customerPhone = doc.getString("customerPhone") ?: ""
                    val customerAddress = doc.getString("customerAddress") ?: ""
                    val paymentMethod = doc.getString("paymentMethod") ?: "Cash on Delivery"
                    val itemsSummary = doc.getString("itemsSummary") ?: ""
                    val subtotal = doc.getDouble("subtotal") ?: 0.0
                    val deliveryFee = doc.getDouble("deliveryFee") ?: 40.0
                    val total = doc.getDouble("total") ?: (subtotal + deliveryFee)
                    val status = doc.getString("status") ?: Order.STATUS_PLACED
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                    Order(
                        id = id,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        customerAddress = customerAddress,
                        paymentMethod = paymentMethod,
                        itemsSummary = itemsSummary,
                        subtotal = subtotal,
                        deliveryFee = deliveryFee,
                        total = total,
                        status = status,
                        createdAt = createdAt
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // Cloud Firestore Sync: Repair Requests
    // ==========================================

    suspend fun uploadRepair(repair: RepairRequest): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        try {
            val doc = mapOf(
                "id" to repair.id,
                "customerName" to repair.customerName,
                "customerPhone" to repair.customerPhone,
                "itemType" to repair.itemType,
                "problemTitle" to repair.problemTitle,
                "problemDescription" to repair.problemDescription,
                "photoUri" to repair.photoUri,
                "preferredDate" to repair.preferredDate,
                "status" to repair.status,
                "estimatedPrice" to repair.estimatedPrice,
                "createdAt" to repair.createdAt
            )
            val docId = repair.id.replace("#", "repair_")
            db.collection(COLLECTION_REPAIRS)
                .document(docId)
                .set(doc, SetOptions.merge())
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadRepairs(repairs: List<RepairRequest>): Result<Int> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        try {
            var count = 0
            for (repair in repairs) {
                uploadRepair(repair)
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadRepairs(): Result<List<RepairRequest>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        try {
            val snapshot = db.collection(COLLECTION_REPAIRS).get().awaitResult()
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: ("#" + doc.id.removePrefix("repair_"))
                    val customerName = doc.getString("customerName") ?: ""
                    val customerPhone = doc.getString("customerPhone") ?: ""
                    val itemType = doc.getString("itemType") ?: "Smartphone"
                    val problemTitle = doc.getString("problemTitle") ?: ""
                    val problemDescription = doc.getString("problemDescription") ?: ""
                    val photoUri = doc.getString("photoUri")
                    val preferredDate = doc.getString("preferredDate") ?: ""
                    val status = doc.getString("status") ?: RepairRequest.STATUS_RECEIVED
                    val estimatedPrice = doc.getDouble("estimatedPrice")
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                    RepairRequest(
                        id = id,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        itemType = itemType,
                        problemTitle = problemTitle,
                        problemDescription = problemDescription,
                        photoUri = photoUri,
                        preferredDate = preferredDate,
                        status = status,
                        estimatedPrice = estimatedPrice,
                        createdAt = createdAt
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension function to await Google Play Services Tasks safely inside Kotlin coroutines.
 */
suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWith(Result.failure(exception))
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
