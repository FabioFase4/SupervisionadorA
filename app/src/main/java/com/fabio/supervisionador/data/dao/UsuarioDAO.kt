package com.fabio.supervisionador.data.dao

import com.fabio.supervisionador.data.model.rnUsuarios
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class rnUsuariosDAO {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usuariosRef: DatabaseReference = database.getReference("usuarios")

    fun adicionarUsuario (uid: String, usuario: rnUsuarios, callback: (Boolean) -> Unit) {
        usuariosRef.child(uid).setValue(usuario)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}
