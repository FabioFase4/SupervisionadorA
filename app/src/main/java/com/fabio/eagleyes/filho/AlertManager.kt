package com.fabio.eagleyes.filho

import com.fabio.eagleyes.alerta.Alerta
import com.fabio.eagleyes.alerta.AlertaRepositorio
import com.fabio.eagleyes.global.FirebaseConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlertManager(private val repositorio: AlertaRepositorio) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val ultimoAlertaPorApp = mutableMapOf<String, Long>()
    private var ultimaVezBypass = 0L

    fun enviarAlertaBloqueio(packageName: String, appLabel: String) {
        val agora = System.currentTimeMillis()
        val ultimaVez = ultimoAlertaPorApp[packageName] ?: 0L

        if (agora - ultimaVez > 5000) { // Anti-spam de 1 minuto
            val emailRaw = FirebaseConfig.getAuth().currentUser?.email ?: ""
            val horario = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val alerta = Alerta(
                id = UUID.randomUUID().toString(),
                aplicativo = appLabel,
                emailFilho = emailRaw,
                horario = horario,
                mensagem = "Acesso ao app $appLabel bloqueado por regra de uso.",
                tipo = "BLOQUEIO",
                timestamp = agora
            )

            scope.launch {
                repositorio.salvarAlerta(alerta)
            }
            ultimoAlertaPorApp[packageName] = agora
        }
    }

    fun enviarAlertaBypass() {
        val agora = System.currentTimeMillis()
        
        // Anti-spam para evitar múltiplas notificações de bypass seguidas
        if (agora - ultimaVezBypass > 5000) {
            val emailRaw = FirebaseConfig.getAuth().currentUser?.email ?: ""
            val horario = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val alerta = Alerta(
                id = UUID.randomUUID().toString(),
                aplicativo = "Configurações",
                emailFilho = emailRaw,
                horario = horario,
                mensagem = "Tentativa de desativar a proteção do EagleEyes detectada.",
                tipo = "SEGURANÇA",
                timestamp = agora
            )

            scope.launch {
                repositorio.salvarAlerta(alerta)
            }
            ultimaVezBypass = agora
        }
    }
}
