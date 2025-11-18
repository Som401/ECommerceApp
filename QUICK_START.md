# Quick Start Checklist

Follow these steps to get your shoe e-commerce app running:

## 1. Firebase Setup (15 minutes)

### A. Create Firebase Project
- [ ] Go to https://console.firebase.google.com
- [ ] Click "Add project"
- [ ] Name it (e.g., "ShoeStore")
- [ ] Disable Google Analytics (optional for development)
- [ ] Click "Create project"

### B. Add Android App
- [ ] Click "Add app" → Android icon
- [ ] Package name: `com.example.e_commerce_app`
- [ ] App nickname: "Shoe E-Commerce"
- [ ] Download `google-services.json`
- [ ] **IMPORTANT**: Move `google-services.json` to `app/` folder

### C. Enable Services
- [ ] Go to "Authentication" → Get Started
- [ ] Click "Sign-in method" tab
- [ ] Enable "Email/Password"
- [ ] Click "Save"

- [ ] Go to "Firestore Database" → Create database
- [ ] Choose "Start in test mode"
- [ ] Select region (closest to you)
- [ ] Click "Enable"

## 2. Add Products to Firebase (10 minutes)

### Method: Firebase Console
- [ ] Open Firestore Database in Firebase Console
- [ ] Click "Start collection"
- [ ] Collection ID: `Products`
- [ ] Click "Next"

### Add First Product (Nike Air Max 270)
- [ ] Document ID: `nike-air-max-270`
- [ ] Add these fields:

| Field | Type | Value |
|-------|------|-------|
| id | string | nike-air-max-270 |
| name | string | Nike Air Max 270 |
| description | string | Comfortable running shoes with great cushioning |
| price | number | 150 |
| discount | number | 10 |
| category | string | Running |
| brand | string | Nike |
| imageUrl | string | https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/i1-665455a5-45de-40fb-945f-c1852b82400d/AIR+MAX+270.png |
| size | array | Add strings: "7", "8", "9", "10", "11" |
| colors | array | Add strings: "Black", "White", "Red" |
| gender | string | Men |
| stock | number | 50 |
| rating | number | 4.5 |

- [ ] Click "Save"

### Add More Products (Optional)
- [ ] See `FIREBASE_DATA_STRUCTURE.md` for 4 more sample products
- [ ] Repeat above process for each product
- [ ] Recommended: Add at least 5-10 products for good demo

## 3. Build and Run (5 minutes)

### Option A: Android Studio
- [ ] Open project in Android Studio
- [ ] Wait for Gradle sync to complete
- [ ] Click green "Run" button
- [ ] Select emulator or connected device
- [ ] Wait for app to install and launch

### Option B: Terminal
```bash
# Set Java 17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# Build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## 4. Test the App (5 minutes)

### Create Account
- [ ] Click "Sign Up"
- [ ] Enter name, email, password
- [ ] Click "Sign Up"
- [ ] Should navigate to home screen

### Browse Products
- [ ] Home tab should show sample products
- [ ] Click "Shop" tab
- [ ] Should see products from Firebase
- [ ] Test category filters (All, Running, Sneakers, etc.)

### Test Cart
- [ ] Add a product to cart (from Shop or Home)
- [ ] Click "Bag" tab
- [ ] Verify product appears
- [ ] Test quantity buttons (+/-)
- [ ] Test remove button
- [ ] Check price calculations

### Test Wishlist
- [ ] Add products to wishlist (heart icon)
- [ ] Click "Favorites" tab
- [ ] Verify products appear
- [ ] Test remove from wishlist

### Test Profile
- [ ] Click "Profile" tab
- [ ] Verify your name and email appear
- [ ] Click "Logout"
- [ ] Should return to login screen

## 5. Troubleshooting

### Build Fails
**Problem**: Gradle sync fails
**Solution**: 
```bash
./gradlew clean
./gradlew assembleDebug
```

**Problem**: Wrong Java version
**Solution**: Ensure Java 17 is installed and set:
```bash
java -version  # Should show version 17
```

### Firebase Issues
**Problem**: Products don't load
**Solution**: 
- Check internet connection
- Verify `google-services.json` is in `app/` folder
- Check Firestore has products added
- Check Firestore rules allow read access

**Problem**: Can't login/signup
**Solution**:
- Verify Email/Password is enabled in Firebase Auth
- Check internet connection
- Look at Logcat for error messages

### App Crashes
**Problem**: App crashes on launch
**Solution**:
- Check Logcat for stack trace
- Verify Firebase configuration
- Ensure all dependencies synced

**Problem**: Empty screens
**Solution**:
- Add products to Firebase
- Check internet connection
- Verify you're logged in

## 6. Firebase Security Rules (Production)

Before publishing, update Firestore rules:

- [ ] Go to Firestore Database → Rules tab
- [ ] Replace with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /Products/{productId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    match /Users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /Cart/{cartId} {
      allow read, write: if request.auth != null && 
                            resource.data.userId == request.auth.uid;
    }
    
    match /Wishlist/{wishlistId} {
      allow read, write: if request.auth != null && 
                            resource.data.userId == request.auth.uid;
    }
  }
}
```

- [ ] Click "Publish"

## Quick Reference

### Important Files
- Firebase config: `app/google-services.json`
- Main activity: `app/src/main/java/.../MainActivity.kt`
- Product data: See Firebase Console
- Documentation: `README.md`, `FIREBASE_DATA_STRUCTURE.md`

### Key Categories (must match exactly)
- Running
- Sneakers
- Sports
- Casual

### Firebase Collections
- Products (read by all)
- Users (read/write by owner)
- Cart (read/write by owner)
- Wishlist (read/write by owner)

### Commands
```bash
# Build
./gradlew assembleDebug

# Install
./gradlew installDebug

# Clean
./gradlew clean

# Check Java version
java -version
```

## Support

### Resources
- Project README: `README.md`
- Firebase guide: `FIREBASE_DATA_STRUCTURE.md`
- Project summary: `PROJECT_SUMMARY.md`

### Common Links
- Firebase Console: https://console.firebase.google.com
- Android Studio: https://developer.android.com/studio
- Material Design: https://material.io/

---

## Estimated Total Time: 35 minutes

✅ Once complete, you'll have a fully functional shoe e-commerce app!
