package com.example.ui.locale

enum class AppLanguage(val code: String, val label: String) {
    EN("en", "English"),
    GU("gu", "ગુજરાતી")
}

class AppStrings(val language: AppLanguage) {
    val isGujarati = language == AppLanguage.GU

    // App Branding
    val appTitle: String = if (isGujarati) "ઇલેક્ટ્રોફિક્સ સ્ટોર" else "ElectroFix Store"
    val appSubtitle: String = if (isGujarati) "સ્પેરપાર્ટ્સ, એક્સેસરીઝ અને રીપેર સેવા" else "Smart Retail & Professional Device Repairs"

    // Navigation
    val navHome: String = if (isGujarati) "હોમ" else "Home"
    val navShop: String = if (isGujarati) "દુકાન" else "Shop"
    val navRepair: String = if (isGujarati) "રીપેર સેવા" else "Repair"
    val navOrders: String = if (isGujarati) "મારા ઓર્ડર" else "Orders"

    // Home Screen
    val buyProductsTitle: String = if (isGujarati) "પ્રોડક્ટ્સ ખરીદો" else "Buy Products"
    val itemsAvailable: String = if (isGujarati) "આઇટમ્સ ઉપલબ્ધ" else "Items Available"
    val browseShop: String = if (isGujarati) "શોપિંગ કરો" else "Browse Shop"
    val repairServiceTitle: String = if (isGujarati) "રીપેર સેવા" else "Repair Service"
    val bookDiagnostic: String = if (isGujarati) "તપાસ અને રીપેર બુક કરો" else "Book Diagnostic & Fix"
    val bookInquiry: String = if (isGujarati) "ઇન્ક્વાયરી બુક કરો" else "Book Inquiry"
    val liveTracking: String = if (isGujarati) "લાઇવ ટ્રેકિંગ અપડેટ" else "Live Activity Tracking"
    val originalSpares: String = if (isGujarati) "ઓરિજિનલ સ્પેરપાર્ટ્સ" else "Original Spares"
    val sameDayFix: String = if (isGujarati) "ઝડપી સર્વિસ" else "Same-Day Fix"
    val warrantyText: String = if (isGujarati) "વોરંટી સપોર્ટ" else "3-Mo Warranty"
    val featuredProducts: String = if (isGujarati) "લોકપ્રિય સ્પેરપાર્ટ્સ અને ગેજેટ્સ" else "Featured Spares & Accessories"
    val seeAll: String = if (isGujarati) "બધા જુઓ" else "See All"
    val brokenScreenBannerTitle: String = if (isGujarati) "સ્ક્રીન તૂટી ગઈ છે કે બેટરીની સમસ્યા છે?" else "Broken Display or Battery Issue?"
    val brokenScreenBannerDesc: String = if (isGujarati) "ફોટો સાથે રીપેર વિનંતી મોકલો અને મફત અંદાજ મેળવો." else "Submit a repair inquiry with a photo and get a free technician quote."
    val bookRepairNow: String = if (isGujarati) "હમણાં રીપેર બુક કરો" else "Book Repair Now"

    // Owner Login & Password Authentication
    val ownerLogin: String = if (isGujarati) "દુકાન માલિક લોગિન" else "Store Owner Login"
    val ownerBadge: String = if (isGujarati) "માલિક પ્રવેશ" else "Owner"
    val enterPin: String = if (isGujarati) "૪-અંકનો પિન દાખલ કરો" else "Enter 4-Digit PIN"
    val enterPassword: String = if (isGujarati) "એડમિન પાસવર્ડ દાખલ કરો" else "Enter Admin Password"
    val passwordLabel: String = if (isGujarati) "એડમિન પાસવર્ડ" else "Admin Password"
    val unlockAdmin: String = if (isGujarati) "એડમિન પેનલ ખોલો" else "Unlock Admin Panel"
    val quickDemoUnlock: String = if (isGujarati) "ડેમો અનલોક (૧૨૩૪)" else "Demo Unlock (1234)"
    val incorrectPin: String = if (isGujarati) "ખોટો પિન. ડિફોલ્ટ પિન ૧૨૩૪ છે" else "Incorrect PIN. Default is 1234"
    val incorrectPassword: String = if (isGujarati) "ખોટો પાસવર્ડ. કૃપા કરીને ફરી પ્રયાસ કરો." else "Incorrect password. Please try again."
    val noPasswordSetNotice: String = if (isGujarati) "હજી સુધી પાસવર્ડ સેટ કરેલ નથી. તમે સીધા લોગિન કરી શકો છો અને પછીથી સેટ કરી શકો છો." else "No password set yet. You can log in directly and set your password later in Admin Settings."
    val setPasswordLaterBtn: String = if (isGujarati) "હમણાં પ્રવેશ કરો (પાસવર્ડ પછીથી સેટ કરો)" else "Enter Now (Set Password Later)"
    val setPasswordNowBtn: String = if (isGujarati) "નવો પાસવર્ડ સેટ કરો" else "Set Admin Password"
    val changePasswordTitle: String = if (isGujarati) "એડમિન પાસવર્ડ સેટ કરો / બદલો" else "Set / Change Admin Password"
    val newPasswordLabel: String = if (isGujarati) "નવો પાસવર્ડ લખો" else "New Password"
    val confirmPasswordLabel: String = if (isGujarati) "પાસવર્ડ ફરી લખો" else "Confirm Password"
    val savePasswordToDb: String = if (isGujarati) "ડેટાબેઝમાં પાસવર્ડ સેવ કરો" else "Save Password to Database"
    val passwordChangedSuccess: String = if (isGujarati) "પાસવર્ડ સફળતાપૂર્વક સાચવવામાં આવ્યો!" else "Admin password saved to database!"
    val passwordsDoNotMatch: String = if (isGujarati) "બંને પાસવર્ડ સરખા નથી" else "Passwords do not match"
    val passwordStatusProtected: String = if (isGujarati) "સુરક્ષિત: કસ્ટમ પાસવર્ડ સક્રિય છે" else "Secured: Custom password active"
    val passwordStatusUnset: String = if (isGujarati) "પાસવર્ડ સેટ નથી (તમે અહીં ક્યારેય પણ સેટ કરી શકો છો)" else "Password not set yet (Set anytime below)"
    val exitAdminMode: String = if (isGujarati) "ગ્રાહક મોડમાં પાછા જાઓ" else "Exit Admin Mode"
    val adminModeBanner: String = if (isGujarati) "👨‍💼 વેપારી મોડ સક્રિય • મેનેજમેન્ટ" else "👨‍💼 Merchant Mode Active • Management"

    // Database Strings
    val databaseTitle: String = if (isGujarati) "રૂમ ડેટાબેઝ મેનેજમેન્ટ (Room SQLite)" else "Room Database Management"
    val databaseSubtitle: String = if (isGujarati) "સ્થાનિક SQLite ડેટાબેઝ - ઑફલાઇન સપોર્ટ અને ઓર્ડર ડેટા" else "Local SQLite Database - Offline Support & Persistent Records"
    val databaseStatusActive: String = if (isGujarati) "ડેટાબેઝ સક્રિય (Room SQLite v2)" else "Database Active (Room SQLite v2)"
    val resetDatabaseBtn: String = if (isGujarati) "સેમ્પલ સ્ટોર ડેટા રીસેટ કરો" else "Reset Sample Store Data"
    val databaseSynced: String = if (isGujarati) "ડેટાબેઝ સુરક્ષિત અને અપ-ટૂ-ડેટ છે" else "Database persistent & up-to-date"

    // Firebase & Cloud Authentication Strings
    val firebaseTitle: String = if (isGujarati) "ફાયરબેઝ ક્લાઉડ & ઓથેન્ટિકેશન" else "Firebase Cloud & Authentication"
    val firebaseSubtitle: String = if (isGujarati) "ક્લાઉડ ફાયરસ્ટોર ડેટાબેઝ અને ગ્રાહક/માલિક સુરક્ષા" else "Cloud Firestore database & Customer/Merchant security"
    val syncToCloudBtn: String = if (isGujarati) "ફાયરસ્ટોરમાં સિંક કરો (Upload)" else "Sync to Cloud Firestore"
    val pullFromCloudBtn: String = if (isGujarati) "ક્લાઉડમાંથી ડાઉનલોડ કરો (Pull)" else "Pull from Cloud Firestore"
    val firebaseAuthCardTitle: String = if (isGujarati) "ફાયરબેઝ એકાઉન્ટ ઓથેન્ટિકેશન" else "Firebase Account Authentication"
    val signInWithEmailBtn: String = if (isGujarati) "ઈમેલ વડે લોગિન કરો" else "Sign In with Email"
    val signUpWithEmailBtn: String = if (isGujarati) "નવું એકાઉન્ટ બનાવો (Sign Up)" else "Create Firebase Account"
    val guestSignInBtn: String = if (isGujarati) "ગેસ્ટ તરીકે પ્રવેશ (Anonymous)" else "Continue as Guest"
    val signOutBtn: String = if (isGujarati) "સાઇન આઉટ" else "Sign Out"
    val emailLabel: String = if (isGujarati) "ઈમેલ એડ્રેસ" else "Email Address"
    val passwordFieldLabel: String = if (isGujarati) "પાસવર્ડ" else "Password"
    val cloudSyncedStatus: String = if (isGujarati) "ક્લાઉડ સિંક થઈ ગયું" else "Cloud Synced"
    val firebaseSetupNotice: String = if (isGujarati)
        "લાઇવ ફાયરબેઝ ક્લાઉડ સિંક માટે તમારા ફાયરબેઝ પ્રોજેક્ટનું google-services.json ફાઇલ /app ફોલ્ડરમાં મૂકો."
    else
        "For live cloud sync across devices, place your Firebase project's google-services.json in the /app folder."

    // Shop Screen
    val shopHeading: String = if (isGujarati) "સ્પેરપાર્ટ્સ અને ગેજેટ્સ" else "Shop Spares & Gadgets"
    val searchPlaceholder: String = if (isGujarati) "પ્રોડક્ટ્સ, સ્ક્રીન, કેબલ શોધો..." else "Search products, screens, cables..."
    val inStockText: String = if (isGujarati) "સ્ટોકમાં ઉપલબ્ધ" else "in stock"
    val outOfStockText: String = if (isGujarati) "સ્ટોક ખલાસ" else "Out of stock"
    val addBtn: String = if (isGujarati) "ઉમેરો" else "Add"
    val saveOff: String = if (isGujarati) "છૂટ" else "OFF"
    val noProductsFound: String = if (isGujarati) "કોઈ પ્રોડક્ટ મળી નથી" else "No products found"

    // Cart & Checkout
    val myCart: String = if (isGujarati) "મારું કાર્ટ" else "My Cart"
    val cartEmpty: String = if (isGujarati) "તમારું કાર્ટ ખાલી છે" else "Your Cart is Empty"
    val cartEmptySubtitle: String = if (isGujarati) "દુકાનમાંથી સ્પેરપાર્ટ્સ અને ચાર્જર કાર્ટમાં ઉમેરો." else "Add replacement parts, chargers, and accessories to your cart."
    val subtotalText: String = if (isGujarati) "સબટોટલ" else "Subtotal"
    val deliveryFeeText: String = if (isGujarati) "ડિલિવરી ચાર્જ" else "Delivery Fee"
    val freeText: String = if (isGujarati) "મફત" else "FREE"
    val totalAmount: String = if (isGujarati) "કુલ રકમ" else "Total Amount"
    val placeOrder: String = if (isGujarati) "ઓર્ડર મુકો" else "PLACE ORDER"
    val checkoutTitle: String = if (isGujarati) "ઓર્ડર ચેકઆઉટ" else "Checkout Order"
    val customerFullName: String = if (isGujarati) "ગ્રાહકનું પૂરું નામ *" else "Customer Full Name *"
    val phoneNumber: String = if (isGujarati) "મોબાઇલ નંબર *" else "Phone Number *"
    val deliveryAddress: String = if (isGujarati) "ડિલિવરી સરનામું / દુકાન પરથી મેળવવું *" else "Delivery Address / Shop Pickup *"
    val paymentMethod: String = if (isGujarati) "ચુકવણી પદ્ધતિ" else "Payment Method"
    val cod: String = if (isGujarati) "કેશ ઓન ડિલિવરી (COD)" else "Cash on Delivery"
    val upiPod: String = if (isGujarati) "ડિલિવરી વખતે UPI થી ચુકવણી" else "Pay on Delivery (UPI)"
    val confirmOrder: String = if (isGujarati) "ઓર્ડર કન્ફર્મ કરો" else "Confirm & Place Order"

    // Orders Screen
    val orderHistoryTitle: String = if (isGujarati) "ઓર્ડર હિસ્ટ્રી અને ટ્રેકિંગ" else "Order History & Tracking"
    val orderHistorySubtitle: String = if (isGujarati) "તમારા ઓર્ડરનું લાઈવ સ્ટેજ ટ્રેકિંગ" else "Track stage-by-stage updates for your shop orders"
    val noOrdersYet: String = if (isGujarati) "કોઈ ઓર્ડર મૂકેલ નથી" else "No orders placed yet"
    val orderTimeline: String = if (isGujarati) "ઓર્ડર ટાઇમલાઇન" else "ORDER TIMELINE"
    val hideTracking: String = if (isGujarati) "ટ્રેકિંગ છુપાવો" else "Hide Tracking"
    val trackOrder: String = if (isGujarati) "ઓર્ડર ટ્રેક કરો" else "Track Order"

    // Repair Screen
    val repairStationTitle: String = if (isGujarati) "રીપેર સર્વિસ સ્ટેશન" else "Repair Service Station"
    val repairStationSubtitle: String = if (isGujarati) "ઝડપી તપાસ, ઓરિજિનલ પાર્ટ્સ અને સ્ટેટસ ટ્રેકિંગ" else "Fast diagnostics, certified spare replacements & status tracking"
    val tabBookRepair: String = if (isGujarati) "રીપેર બુક કરો" else "Book a Repair"
    val tabMyRepairs: String = if (isGujarati) "મારા રીપેર" else "My Repairs"
    val selectDeviceCategory: String = if (isGujarati) "ડિવાઇસ કેટેગરી પસંદ કરો" else "Select Device Category"
    val whatIsProblem: String = if (isGujarati) "મુખ્ય સમસ્યા શું છે? *" else "What is the main problem? *"
    val describeIssue: String = if (isGujarati) "સમસ્યાનું વિગતવાર વર્ણન કરો *" else "Describe the issue in detail *"
    val uploadPhotoOptional: String = if (isGujarati) "ફોટો અપલોડ કરો (વૈકલ્પિક)" else "Upload Photo (Optional)"
    val attachPhotoText: String = if (isGujarati) "નુકસાન / ખામીનો ફોટો જોડો" else "Attach photo of damage / issue"
    val yourName: String = if (isGujarati) "તમારું નામ *" else "Your Name *"
    val phoneForUpdates: String = if (isGujarati) "અપડેટ માટે ફોન નંબર *" else "Phone Number for Status Updates *"
    val preferredAppointmentTime: String = if (isGujarati) "અનુકૂળ મુલાકાત / ડિવાઇસ આપવાનો સમય" else "Preferred Appointment / Drop-off Time"
    val submitRepairRequestBtn: String = if (isGujarati) "રીપેર વિનંતી સબમિટ કરો" else "SUBMIT REPAIR REQUEST"
    val noRepairsYet: String = if (isGujarati) "કોઈ રીપેર વિનંતી નથી" else "No repair requests submitted yet"
    val repairProgress: String = if (isGujarati) "રીપેર પ્રગતિ" else "REPAIR PROGRESS"
    val estimatedCost: String = if (isGujarati) "અંદાજિત ખર્ચ:" else "Estimated Cost:"

    // Helper functions for category and status translation
    fun translateCategory(category: String): String {
        if (!isGujarati) return category
        return when (category.lowercase()) {
            "all" -> "બધા"
            "screens & parts" -> "સ્ક્રીન અને પાર્ટ્સ"
            "batteries & power" -> "બેટરી અને પાવર"
            "chargers & cables" -> "ચાર્જર અને કેબલ"
            "audio & gadgets" -> "ઓડિયો અને ગેજેટ્સ"
            "pc & storage" -> "પીસી અને સ્ટોરેજ"
            "repair tools" -> "રીપેર ટૂલ્સ"
            else -> category
        }
    }

    fun translateOrderStatus(status: String): String {
        if (!isGujarati) return status
        return when (status.lowercase()) {
            "order placed" -> "ઓર્ડર મુકાયો"
            "confirmed" -> "પુષ્ટિ થઈ"
            "packed" -> "પેક થયું"
            "shipped" -> "ડિસ્પેચ થયું"
            "delivered" -> "ડિલિવરી થઈ"
            else -> status
        }
    }

    fun translateRepairStatus(status: String): String {
        if (!isGujarati) return status
        return when (status.lowercase()) {
            "request received" -> "વિનંતી મળી"
            "contacted" -> "સંપર્ક કર્યો"
            "repairing" -> "રીપેરીંગ ચાલુ છે"
            "ready" -> "તૈયાર છે"
            "completed" -> "પૂર્ણ થયું"
            else -> status
        }
    }

    fun translateItemType(itemType: String): String {
        if (!isGujarati) return itemType
        return when (itemType.lowercase()) {
            "smartphone" -> "સ્માર્ટફોન"
            "laptop & pc" -> "લેપટોપ અને પીસી"
            "tablet / ipad" -> "ટેબ્લેટ / આઈપેડ"
            "smartwatch" -> "સ્માર્ટવોચ"
            "audio & headphones" -> "ઓડિયો અને હેડફોન"
            "tv & monitor" -> "ટીવી અને મોનિટર"
            "gaming console" -> "ગેમિંગ કન્સોલ"
            "other device" -> "અન્ય ડિવાઇસ"
            else -> itemType
        }
    }
}
