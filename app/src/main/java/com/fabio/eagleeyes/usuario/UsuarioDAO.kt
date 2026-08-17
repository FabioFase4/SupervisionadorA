package com.fabio.eagleeyes.usuario

import com.fabio.eagleeyes.alerta.Alerta
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class UsuarioDAO {
    private val auth = FirebaseConfig.getAuth()
    private val rootDb = FirebaseConfig.getDatabase()

    /**
     * Realiza o login via Firebase Auth.
     */
    suspend fun login(email: String, senhaPura: String): String? {
        return try {
            val resultado = auth.signInWithEmailAndPassword(email, senhaPura).await()
            val uid = resultado.user?.uid ?: return null

            // Tenta buscar no nó "usuarios/pais"
            val snapshotPai = rootDb.child("usuarios/pais").child(email.replace(".", ",")).get().await()
            if (snapshotPai.exists()) return "pai"

            // Tenta buscar no nó "usuarios/filhos"
            val snapshotFilho = rootDb.child("usuarios/filhos").child(email.replace(".", ",")).get().await()
            if (snapshotFilho.exists()) return "filho"

            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cria o usuário no Firebase Auth e salva os dados adicionais no Realtime Database
     */
    suspend fun cadastrar(usuario: Usuario, local: String): String? {
        return try {
            if (emailExiste(usuario.email))
                throw Exception("E-mail já cadastrado")

            if (telefoneExiste(usuario.telefone))
                throw Exception("Telefone já cadastrado")

            val resultado = auth.createUserWithEmailAndPassword(usuario.email, usuario.senha).await()
            val uid = resultado.user?.uid ?: return null

            val emailChave = usuario.email.replace(".", ",")
            rootDb.child(local).child(emailChave).setValue(usuario).await()
            uid
        } catch (e: Exception) {
            null
        }
    }


    suspend fun emailExiste(email: String): Boolean {
        val emailChave = email.replace(".", ",")
        val paiExiste = rootDb.child("usuarios/pais").child(emailChave).get().await().exists()
        val filhoExiste = rootDb.child("usuarios/filhos").child(emailChave).get().await().exists()
        return paiExiste || filhoExiste
    }

    suspend fun telefoneExiste(telefone: String): Boolean {
        // Busca em 'pais'
        val snapshotPais = rootDb.child("usuarios/pais")
            .orderByChild("telefone")
            .equalTo(telefone)
            .get().await()

        if (snapshotPais.exists()) return true

        // Busca em 'filhos'
        val snapshotFilhos = rootDb.child("usuarios/filhos")
            .orderByChild("telefone")
            .equalTo(telefone)
            .get().await()

        return snapshotFilhos.exists()
    }

    /**
     * Busca os filhos associados ao e-mail do pai
     */
    suspend fun listarFilhosDoPai(emailDoPai: String): List<Usuario> {
        return try {
            val snapshot = rootDb.child("usuarios/filhos")
                .orderByChild("emailResponsavel")
                .equalTo(emailDoPai)
                .get().await()

            snapshot.children.mapNotNull { it.getValue(Usuario::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Escuta em tempo real os alertas gerados para o pai logado.
     * Importante: Usa o email sanitizado como chave no nó "alertas".
     */
    fun escutarAlertasDoPai(onAlertasAtualizados: (List<Alerta>) -> Unit, onErro: (String) -> Unit) {
        val user = auth.currentUser ?: return
        val emailPai = user.email ?: return
        val emailChave = emailPai.replace(".", ",")

        rootDb.child("alertas").child(emailChave).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaAlertas = mutableListOf<Alerta>()

                for (alertaSnapshot in snapshot.children) {
                    val alerta = alertaSnapshot.getValue(Alerta::class.java)
                    if (alerta != null) {
                        listaAlertas.add(alerta.copy(id = alertaSnapshot.key ?: ""))
                    }
                }
                onAlertasAtualizados(listaAlertas)
            }

            override fun onCancelled(error: DatabaseError) {
                onErro(error.message)
            }
        })
    }
}