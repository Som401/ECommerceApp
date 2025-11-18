# Firebase Data Structure for Shoe E-Commerce App

This document describes how to manually add products to your Firebase Firestore database.

## Collections

### 1. Products Collection
**Collection Path**: `Products`

Each product document should have the following structure:

```json
{
  "id": "unique-product-id",
  "name": "Nike Air Max 270",
  "description": "Comfortable running shoes with great cushioning",
  "price": 150.00,
  "discount": 10,
  "category": "Running",
  "brand": "Nike",
  "imageUrl": "https://example.com/product-image.jpg",
  "size": ["7", "8", "9", "10", "11"],
  "colors": ["Black", "White", "Red"],
  "gender": "Men",
  "stock": 50,
  "rating": 4.5
}
```

**Field Descriptions**:
- `id` (String): Unique identifier for the product
- `name` (String): Product name
- `description` (String): Product description
- `price` (Number): Base price in USD
- `discount` (Number): Discount percentage (0-100)
- `category` (String): One of: "Running", "Sneakers", "Sports", "Casual"
- `brand` (String): Brand name (e.g., "Nike", "Adidas", "Puma")
- `imageUrl` (String): URL to product image
- `size` (Array of Strings): Available sizes
- `colors` (Array of Strings): Available colors
- `gender` (String): "Men", "Women", or "Unisex"
- `stock` (Number): Number of items in stock
- `rating` (Number): Rating from 1-5

### 2. Users Collection
**Collection Path**: `Users`

Created automatically when users sign up:

```json
{
  "id": "user-uid",
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "timestamp"
}
```

### 3. Cart Collection
**Collection Path**: `Cart`

Created automatically when users add items to cart:

```json
{
  "id": "cart-item-id",
  "userId": "user-uid",
  "productId": "product-id",
  "productName": "Nike Air Max 270",
  "price": 135.00,
  "selectedSize": "9",
  "selectedColor": "Black",
  "quantity": 2,
  "imageUrl": "https://example.com/product-image.jpg"
}
```

### 4. Wishlist Collection
**Collection Path**: `Wishlist`

Created automatically when users add items to wishlist:

```json
{
  "id": "userId_productId",
  "userId": "user-uid",
  "productId": "product-id",
  "addedAt": "timestamp"
}
```

## How to Add Products Manually in Firebase Console

1. Go to Firebase Console (https://console.firebase.google.com)
2. Select your project
3. Navigate to Firestore Database
4. Click "Start Collection"
5. Enter collection ID: `Products`
6. Add Document ID (use the product id, e.g., "nike-air-max-270")
7. Add fields one by one:
   - For simple fields: Click "Add field", enter name and value
   - For arrays (size, colors): Select type "array" and add items
   - For numbers: Select type "number"
   - For strings: Select type "string"

## Sample Products to Add

### Product 1: Nike Air Max 270
```
Document ID: nike-air-max-270

Fields:
- id: "nike-air-max-270" (string)
- name: "Nike Air Max 270" (string)
- description: "Comfortable running shoes with great cushioning" (string)
- price: 150 (number)
- discount: 10 (number)
- category: "Running" (string)
- brand: "Nike" (string)
- imageUrl: "https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/i1-665455a5-45de-40fb-945f-c1852b82400d/AIR+MAX+270.png" (string)
- size: ["7", "8", "9", "10", "11"] (array)
- colors: ["Black", "White", "Red"] (array)
- gender: "Men" (string)
- stock: 50 (number)
- rating: 4.5 (number)
```

### Product 2: Adidas Ultraboost
```
Document ID: adidas-ultraboost

Fields:
- id: "adidas-ultraboost" (string)
- name: "Adidas Ultraboost 22" (string)
- description: "Premium running shoes with boost technology" (string)
- price: 180 (number)
- discount: 15 (number)
- category: "Running" (string)
- brand: "Adidas" (string)
- imageUrl: "https://assets.adidas.com/images/w_600,f_auto,q_auto/0d63a8859a2f481b8c8fad7500f8d54c_9366/Ultraboost_22_Shoes_Black_GZ0127_01_standard.jpg" (string)
- size: ["7", "8", "9", "10", "11", "12"] (array)
- colors: ["Black", "White", "Blue"] (array)
- gender: "Unisex" (string)
- stock: 30 (number)
- rating: 4.7 (number)
```

### Product 3: Puma RS-X
```
Document ID: puma-rs-x

Fields:
- id: "puma-rs-x" (string)
- name: "Puma RS-X³" (string)
- description: "Retro-inspired sneakers with modern comfort" (string)
- price: 110 (number)
- discount: 20 (number)
- category: "Sneakers" (string)
- brand: "Puma" (string)
- imageUrl: "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/371570/01/sv01/fnd/IND/fmt/png/RS-X³-Puzzle-Sneakers" (string)
- size: ["6", "7", "8", "9", "10", "11"] (array)
- colors: ["White", "Black", "Multi"] (array)
- gender: "Unisex" (string)
- stock: 40 (number)
- rating: 4.3 (number)
```

### Product 4: New Balance 574
```
Document ID: new-balance-574

Fields:
- id: "new-balance-574" (string)
- name: "New Balance 574 Classic" (string)
- description: "Iconic casual sneakers with timeless style" (string)
- price: 85 (number)
- discount: 0 (number)
- category: "Casual" (string)
- brand: "New Balance" (string)
- imageUrl: "https://nb.scene7.com/is/image/NB/ml574evg_nb_02_i?$pdpflexf2$&qlt=80&fmt=webp&wid=440&hei=440" (string)
- size: ["7", "8", "9", "10", "11"] (array)
- colors: ["Grey", "Navy", "Burgundy"] (array)
- gender: "Unisex" (string)
- stock: 60 (number)
- rating: 4.6 (number)
```

### Product 5: Jordan 1 Retro
```
Document ID: jordan-1-retro

Fields:
- id: "jordan-1-retro" (string)
- name: "Air Jordan 1 Retro High" (string)
- description: "Classic basketball sneakers with iconic design" (string)
- price: 170 (number)
- discount: 5 (number)
- category: "Sneakers" (string)
- brand: "Jordan" (string)
- imageUrl: "https://static.nike.com/a/images/c_limit,w_592,f_auto/t_product_v1/48fc8eba-fd2a-4d8d-a3e3-7e8f6c85c9d4/air-jordan-1-retro-high-og-shoe-Pz1d9F.png" (string)
- size: ["7", "8", "9", "10", "11", "12"] (array)
- colors: ["Black/Red", "White/Black", "Blue"] (array)
- gender: "Unisex" (string)
- stock: 25 (number)
- rating: 4.8 (number)
```

## Notes

- Make sure all product IDs are unique
- Categories must match exactly: "Running", "Sneakers", "Sports", or "Casual" for the filters to work
- Gender should be: "Men", "Women", or "Unisex"
- Image URLs should be publicly accessible
- Prices are in USD
- Discount is a percentage (0-100)
- Stock should be a positive number
- Rating should be between 0 and 5

## Security Rules

Make sure your Firestore security rules allow read access to Products collection:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Products - readable by all, writable by authenticated users (or admin only)
    match /Products/{productId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Users - only owner can read/write their own data
    match /Users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Cart - only owner can read/write their own cart items
    match /Cart/{cartId} {
      allow read, write: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Wishlist - only owner can read/write their own wishlist items
    match /Wishlist/{wishlistId} {
      allow read, write: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}
```
