package com.example.e_commerce_app.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
    val currentUser get() = auth.currentUser
    val isUserLoggedIn get() = currentUser != null
}
