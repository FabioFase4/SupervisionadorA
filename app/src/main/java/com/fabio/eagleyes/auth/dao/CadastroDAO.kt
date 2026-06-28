package com.fabio.eagleyes.auth.dao

import android.util.Log

import com.fabio.eagleyes.auth.model.Usuario
import com.fabio.eagleyes.global.FirebaseConfig

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import org.mindrot.jbcrypt.BCrypt

class CadastroDAO {
    private val auth = FirebaseConfig.getAuth()
    private val db = FirebaseConfig.getDatabase()

    /**
     * Salva o usuário no Auth e no Database com verificação de segurança.
     */
    suspend fun salvarUsuario(usuario: Usuario, noDestino: String): Boolean {
        val emailFormatado = usuario.email.lowercase().trim()
        val senhaPura = usuario.senha

        // 1. Cria o usuário no Firebase Authentication primeiro.
        // Isso faz com que o usuário fique LOGADO, permitindo passar pela regra 'auth != null'.
        val resultado = auth.createUserWithEmailAndPassword(emailFormatado, senhaPura).await()
        val uid = resultado.user?.uid ?: throw Exception("Não foi possível criar a autenticação.")

        try {
            // 2. Agora logado, verifica se o telefone já existe
            verificarTelefoneDuplicado(usuario.telefone)

            // 3. Criptografia e formatação
            val senhaCriptografada = withContext(Dispatchers.Default) {
                BCrypt.hashpw(senhaPura, BCrypt.gensalt())
            }

            usuario.uid = uid
            usuario.email = emailFormatado
            usuario.senha = senhaCriptografada
            if (!usuario.emailResponsavel.isNullOrEmpty()) {
                usuario.emailResponsavel = usuario.emailResponsavel.lowercase().trim()
            }

            // 4. Salva os dados no Database
            db.child(noDestino).child(uid).setValue(usuario).await()

            Log.d("CADASTRO_DEBUG", "Usuário $uid salvo com sucesso")
            return true

        } catch (e: Exception) {
            // Se houver qualquer erro (como telefone duplicado), removemos o usuário do Auth
            // para manter a consistência entre Auth e Database.
            auth.currentUser?.delete()?.await()
            throw e
        }
    }

    private suspend fun verificarTelefoneDuplicado(telefone: String) {
        val nos = listOf("pais", "filhos")
        for (no in nos) {
            val snapshot = db.child(no)
                .orderByChild("telefone")
                .equalTo(telefone)
                .get()
                .await()
            
            if (snapshot.exists()) {
                throw Exception("Este número de telefone já está cadastrado.")
            }
        }
    }
}
