package com.example.swiftcause.utils

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

object FirebaseManager {
    val auth: FirebaseAuth by lazy { Firebase.auth }
    val firestore: FirebaseFirestore by lazy { Firebase.firestore }
}
