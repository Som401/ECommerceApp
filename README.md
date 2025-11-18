# Shoe E-Commerce Android App

A complete Android e-commerce application for selling shoes, built with Kotlin and Firebase.

## Features

- **User Authentication**
  - Email/Password sign up and login
  - Firebase Authentication integration
  - Real-time input validation
  - Secure user session management

- **Product Browsing**
  - Home page with featured products and new arrivals
  - Shop page with category filters (Running, Sneakers, Sports, Casual)
  - Product display with images, prices, ratings, and discounts
  - Grid layout for better product visibility

- **Shopping Cart**
  - Add products to cart with size and color selection
  - Update quantities
  - Remove items
  - Real-time price calculations (subtotal, shipping, total)
  - Persistent cart storage in Firebase

- **Wishlist**
  - Save favorite products
  - Easy removal from wishlist
  - Synchronized across devices via Firebase

- **User Profile**
  - View user information
  - Logout functionality
  - Placeholders for orders, addresses, and payment methods

## Tech Stack

### Frontend
- **Language**: Kotlin
- **UI**: XML Layouts with Material Design Components
- **Architecture**: Fragment-based navigation with ViewBinding
- **Async**: Kotlin Coroutines

### Backend
- **Authentication**: Firebase Authentication
- **Database**: Cloud Firestore
- **Storage**: Firebase Storage (for product images)

### Build Tools
- **Gradle**: 8.13
- **Java**: 17
- **Compile SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)

## Project Structure

```
app/src/main/java/com/example/e_commerce_app/
├── data/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Product.kt
│   │   ├── CartItem.kt
│   │   └── WishlistItem.kt
│   └── repository/
│       └── ProductRepository.kt
├── ui/
│   ├── activities/
│   │   ├── MainActivity.kt
│   │   ├── LoginActivity.kt
│   │   └── SignUpActivity.kt
│   ├── adapters/
│   │   ├── ProductAdapter.kt
│   │   ├── ProductGridAdapter.kt
│   │   └── CartAdapter.kt
│   └── fragments/
│       ├── HomeFragment.kt
│       ├── ShopFragment.kt
│       ├── BagFragment.kt
│       ├── FavoritesFragment.kt
│       └── ProfileFragment.kt
└── utils/
    ├── FirebaseManager.kt
    └── Extensions.kt
```

## Setup Instructions

### Prerequisites
- Android Studio (latest version recommended)
- Java 17 installed
- Firebase project created

### 1. Clone the Repository
```bash
git clone <repository-url>
cd ECommerceApp
```

### 2. Firebase Configuration
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project or use existing one
3. Add an Android app to your Firebase project
   - Package name: `com.example.e_commerce_app`
4. Download `google-services.json`
5. Place it in `app/` directory

### 3. Enable Firebase Services
In Firebase Console, enable:
- **Authentication**: Email/Password provider
- **Firestore Database**: Start in test mode (update rules later)
- **Storage**: For product images

### 4. Add Products to Firebase
Follow the guide in `FIREBASE_DATA_STRUCTURE.md` to manually add products to Firestore.

Sample product structure:
```json
{
  "id": "nike-air-max-270",
  "name": "Nike Air Max 270",
  "description": "Comfortable running shoes",
  "price": 150.00,
  "discount": 10,
  "category": "Running",
  "brand": "Nike",
  "imageUrl": "https://...",
  "size": ["7", "8", "9", "10", "11"],
  "colors": ["Black", "White", "Red"],
  "gender": "Men",
  "stock": 50,
  "rating": 4.5
}
```

### 5. Build and Run
```bash
# Set Java 17 as JAVA_HOME
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# Build the app
./gradlew assembleDebug

# Or run directly from Android Studio
```

## Firebase Security Rules

Update your Firestore security rules for production:

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

## App Architecture

### Data Flow
1. **User Authentication**: LoginActivity/SignUpActivity → Firebase Auth → MainActivity
2. **Product Display**: Fragment → ProductRepository → Firestore → UI Update
3. **Cart Management**: BagFragment → ProductRepository → Firestore → UI Update
4. **Wishlist**: FavoritesFragment → ProductRepository → Firestore → UI Update

### Key Components

#### ProductRepository
Central data management class handling:
- Product CRUD operations
- Cart management
- Wishlist operations
- Firebase Firestore integration

#### Fragments
- **HomeFragment**: Featured products and new arrivals
- **ShopFragment**: All products with category filtering
- **BagFragment**: Shopping cart with quantity management
- **FavoritesFragment**: User's wishlist
- **ProfileFragment**: User information and settings

#### Adapters
- **ProductAdapter**: Horizontal scrolling product lists
- **ProductGridAdapter**: Grid layout for shop/wishlist
- **CartAdapter**: Cart items with quantity controls

## Dependencies

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")

// Material Design
implementation("com.google.android.material:material:1.9.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

// AndroidX
implementation("androidx.core:core-ktx:1.9.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
implementation("androidx.recyclerview:recyclerview:1.3.0")
```

## Features Roadmap

### Completed ✅
- User authentication (signup/login)
- Product browsing with categories
- Shopping cart functionality
- Wishlist management
- User profile
- Firebase integration

### Future Enhancements 🚀
- [ ] Product detail page
- [ ] Size and color selection UI
- [ ] Checkout and payment integration
- [ ] Order history
- [ ] Product search functionality
- [ ] Filters (price range, brand, size)
- [ ] Product reviews and ratings
- [ ] Push notifications
- [ ] Image caching with Glide/Coil
- [ ] Offline support
- [ ] Admin panel for product management

## Testing

### Test User Account
For testing, create a user account through the app or use Firebase Console to add test users.

### Sample Products
See `FIREBASE_DATA_STRUCTURE.md` for sample products to add for testing.

## Troubleshooting

### Build Errors
- Ensure Java 17 is being used: `java -version`
- Clean and rebuild: `./gradlew clean assembleDebug`
- Invalidate caches in Android Studio

### Firebase Issues
- Verify `google-services.json` is in correct location
- Check Firebase Console for enabled services
- Ensure package name matches in Firebase and app

### Runtime Crashes
- Check Logcat for errors
- Verify Firebase rules allow read/write access
- Ensure products exist in Firestore database

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to the branch
5. Create a Pull Request

## License

This project is for educational purposes.

## Author

Created as part of ESILV coursework.

## Screenshots

_Add screenshots of your app here once running_

## Contact

For questions or support, please contact the development team.
