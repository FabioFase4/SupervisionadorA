package com.fabio.eagleyes.alerta

import android.util.Log
import com.fabio.eagleyes.global.FirebaseConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class AlertaRepositorio(
    private val alertaDao: AlertaDAO = AlertaDAO()
) {
    private val auth = FirebaseConfig.getAuth()
    private val rootRef = FirebaseConfig.getDatabase()

    // Escuta alertas em tempo real. Identifica automaticamente se o usuário logado é pai ou filho
    // para buscar os alertas vinculados ao responsável correto.
    fun escutarAlertas(
        emailFilho: String? = null,
        onResult: (List<Alerta>) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onError("Usuário não autenticado")
            return
        }

        val userId = user.uid

        rootRef.child("usuarios").get().addOnSuccessListener { snapshot ->
            var emailPai: String? = null
            val isPai = snapshot.child("pais").child(userId).exists()

            if (isPai)
                emailPai = user.email
            else {
                val filhoSnapshot = snapshot.child("filhos").child(userId)
                if (filhoSnapshot.exists())
                    emailPai = filhoSnapshot.child("emailResponsavel").getValue(String::class.java)
            }

            if (emailPai == null) {
                onError("Não foi possível identificar o responsável.")
                return@addOnSuccessListener
            }

            // Define o filtro: se for filho logado, vê apenas os seus. Se for pai, pode ver todos ou um específico.
            val filtroFilho = emailFilho ?: if (!isPai) user.email else null
            val ref = alertaDao.getAlertasRef(emailPai, filtroFilho)

            Log.d("AlertaRepositorio", "Escutando alertas em: ${ref.path}")

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = mutableListOf<Alerta>()
                    
                    if (!filtroFilho.isNullOrEmpty()) {
                        // Lista de um filho específico
                        for (alertaSnap in snapshot.children) {
                            alertaSnap.getValue(Alerta::class.java)?.let {
                                lista.add(it.copy(id = alertaSnap.key ?: ""))
                            }
                        }
                    } else {
                        // Lista geral do pai (percorre todos os filhos)
                        for (filhoSnap in snapshot.children) {
                            for (alertaSnap in filhoSnap.children) {
                                alertaSnap.getValue(Alerta::class.java)?.let {
                                    lista.add(it.copy(id = alertaSnap.key ?: ""))
                                }
                            }
                        }
                    }
                    onResult(lista)
                }

                override fun onCancelled(error: DatabaseError) = onError(error.message)
            })
        }
    }

    // Salva um alerta, detectando automaticamente o vínculo entre pai e filho.

    suspend fun salvarAlerta(alerta: Alerta): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            
            // Busca informações do usuário para definir emailPai e emailFilho
            val snapshotFilho = rootRef.child("usuarios/filhos").child(user.uid).get().await()

            var emailPai: String?
            var emailFilho: String

            if (snapshotFilho.exists()) {
                emailPai = snapshotFilho.child("emailResponsavel").getValue(String::class.java)
                emailFilho = user.email ?: ""
            } else {
                emailPai = user.email
                emailFilho = alerta.emailFilho
            }

            if (emailPai.isNullOrEmpty() || emailFilho.isEmpty()) return false

            alertaDao.salvarAlerta(emailPai, emailFilho, alerta)
        } catch (e: Exception) {
            Log.e("AlertaRepositorio", "Erro ao salvar: ${e.message}")
            false
        }
    }
}
