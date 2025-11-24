# Coroutines and Implicit Intents Documentation

## ✅ COROUTINES USAGE IN PROJECT

### 1. **Currency Exchange API Call** (CurrencyConverter.kt)
**Location:** `/app/src/main/java/com/example/e_commerce_app/utils/CurrencyConverter.kt`

```kotlin
suspend fun fetchExchangeRate(): Double = withContext(Dispatchers.IO) {
    try {
        val response = URL(API_URL).readText()
        val json = JSONObject(response)
        val rates = json.getJSONObject("rates")
        usdToEurRate = rates.getDouble("EUR")
        lastFetchTime = System.currentTimeMillis()
        usdToEurRate
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching exchange rate: ${e.message}", e)
        usdToEurRate
    }
}
```

**Purpose:** Fetches real-time currency exchange rates from API
**Context:** Uses `Dispatchers.IO` for network operations
**Called from:** ShopFragment.loadUserCurrency()

### 2. **Firestore Database Operations** (Multiple locations)
**Examples:**
- `CartCache.getCartItems()` - Line ~25
- `WishlistCache.getWishlistProducts()` - Line ~81
- `CheckoutActivity.placeOrder()` - Line ~113
- `OrdersActivity.loadOrders()` - Line ~51

**Purpose:** Asynchronous database reads/writes
**Context:** Uses `lifecycleScope.launch` with coroutines
**Pattern:** All use `suspend` functions with `await()` for Firebase operations

---

## ✅ IMPLICIT INTENTS USAGE IN PROJECT

### 1. **Image Picker Intent** (ProfileFragment.kt)
**Location:** `/app/src/main/java/com/example/e_commerce_app/ui/fragments/ProfileFragment.kt` - Line 132

```kotlin
private fun openGallery() {
    val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    pickImageLauncher.launch(pickPhotoIntent)
}
```

**Purpose:** Opens system gallery to select profile photo
**Type:** Implicit Intent (ACTION_PICK)
**Result:** Returns selected image URI

### 2. **Camera Intent** (ProfileFragment.kt)
**Location:** `/app/src/main/java/com/example/e_commerce_app/ui/fragments/ProfileFragment.kt` - Line 127

```kotlin
private fun openCamera() {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    takePictureLauncher.launch(takePictureIntent)
}
```

**Purpose:** Opens system camera to take profile photo
**Type:** Implicit Intent (ACTION_IMAGE_CAPTURE)
**Result:** Returns captured image bitmap

### 3. **Share Order Intent** (NEW - OrdersActivity.kt)
**Location:** Added to demonstrate explicit implicit intent usage

```kotlin
private fun shareOrderDetails(order: Order) {
    val shareText = "Order #${order.id.takeLast(8)}\\n" +
                    "Total: ${CurrencyConverter.formatPrice(order.total, \"USD\")}\\n" +
                    "Status: ${order.status}"
    
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "My Order Details")
    }
    
    startActivity(Intent.createChooser(shareIntent, "Share order via"))
}
```

**Purpose:** Share order details via any app (email, messaging, etc.)
**Type:** Implicit Intent (ACTION_SEND)
**Result:** Opens system share sheet

---

## Summary

✅ **Coroutines:** Multiple instances throughout the project for:
   - Network API calls (Currency exchange)
   - Firebase Firestore operations
   - Asynchronous UI updates

✅ **Implicit Intents:** 3 instances:
   1. Gallery picker (ACTION_PICK)
   2. Camera capture (ACTION_IMAGE_CAPTURE)  
   3. Share functionality (ACTION_SEND)

All requirements are met!
