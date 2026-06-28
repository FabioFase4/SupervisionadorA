package com.fabio.eagleyes.pai

import android.util.Log

import com.fabio.eagleyes.global.FirebaseConfig
import com.fabio.eagleyes.auth.model.Usuario
import com.google.firebase.database.DatabaseReference

import kotlinx.coroutines.tasks.await

class HomePaiDAO (private val database: DatabaseReference = FirebaseConfig.getDatabase()){
    suspend fun listarFilhos(emailDoPai: String): List<Usuario> {
        return try {
            val emailBusca = emailDoPai.trim()

            Log.d("HOME_PAI_DEBUG", "Buscando na rota 'usuarios/filhos' por emailResponsavel == $emailBusca")

            val snapshot = database.child("usuarios/filhos")
                .orderByChild("emailResponsavel")
                .equalTo(emailBusca)
                .get().await()

            val lista = ArrayList<Usuario>()

            if (snapshot.exists()) {
                for (filhoSnapshot in snapshot.children) {
                    val filho = filhoSnapshot.getValue(Usuario::class.java)
                    if (filho != null) {
                        // Importante: Guardamos a chave (UID ou EmailKey) no objeto Usuario
                        filho.uid = filhoSnapshot.key ?: ""
                        lista.add(filho)
                    }
                }
                Log.d("HOME_PAI_DEBUG", "Sucesso! Foram encontrados ${lista.size} filho(s)")
            } else {
                Log.w("HOME_PAI_DEBUG", "Nenhum filho encontrado")
            }
            lista
        } catch (e: Exception) {
            Log.e("HOME_PAI_DEBUG", "Erro ao acessar o Firebase: ${e.message}")
            emptyList()
        }
    }
}
