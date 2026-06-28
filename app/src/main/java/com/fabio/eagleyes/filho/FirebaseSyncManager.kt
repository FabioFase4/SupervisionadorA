package com.fabio.eagleyes.filho

import com.fabio.eagleyes.funcionalidades.regra.Regra
import com.fabio.eagleyes.global.FirebaseConfig
import com.google.firebase.database.*

class FirebaseSyncManager(private val emailFilhoRaw: String) {
    private val database = FirebaseConfig.getDatabase()
    private val emailFormatado = emailFilhoRaw.replace(".", ",")
    
    private val _regrasAtivas = mutableListOf<Regra>()
    val regrasAtivas: List<Regra> get() = _regrasAtivas

    private val _termosBloqueio = mutableListOf<String>()
    val termosBloqueio: List<String> get() = _termosBloqueio

    private val _nomesApp = mutableListOf<String>()
    val nomesApp: List<String> get() = _nomesApp

    fun startSync() {
        // Monitora as regras específicas do filho
        database.child("regras").child(emailFormatado).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _regrasAtivas.clear()
                for (regraSnapshot in snapshot.children) {
                    val regra = regraSnapshot.getValue(Regra::class.java)
                    if (regra != null && regra.ativa) _regrasAtivas.add(regra)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Monitora termos globais de proteção (Hacks/Botões de sistema)
        val refProtecao = database.child("config_protecao")
        
        refProtecao.child("termos_bloqueio").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _termosBloqueio.clear()
                if (snapshot.exists()) {
                    for (termoSnapshot in snapshot.children) {
                        val termo = termoSnapshot.getValue(String::class.java)
                        if (termo != null) _termosBloqueio.add(termo)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Monitora nomes que identificam o app nas configurações
        refProtecao.child("nomes_app").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _nomesApp.clear()
                if (snapshot.exists()) {
                    for (nomeSnapshot in snapshot.children) {
                        val nome = nomeSnapshot.getValue(String::class.java)
                        if (nome != null) _nomesApp.add(nome)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}