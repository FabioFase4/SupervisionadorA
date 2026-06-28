package com.fabio.eagleyes.global

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseConfig {

    fun getAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    fun getDatabase(): DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getAlertasRef(): DatabaseReference = getDatabase().child("alertas")
    fun getFilhosRef(): DatabaseReference = getDatabase().child("filhos")
    fun getPaisRef(): DatabaseReference = getDatabase().child("pais")
}