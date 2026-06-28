package com.fabio.eagleyes.funcionalidades.regra

import com.fabio.eagleyes.global.FirebaseConfig
import com.google.firebase.database.DatabaseReference

class RegraDAO {
    private val rootDb = FirebaseConfig.getDatabase()

    fun noRegrasFilho (uidFilho: String): DatabaseReference {
        return rootDb.child("regras").child(uidFilho)
    }
}
