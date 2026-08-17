package com.fabio.eagleeyes.funcionalidades.monitoramento

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

import com.fabio.eagleeyes.alerta.AlertaRepositorio
import com.fabio.eagleeyes.alerta.comportamento.AlertManager
import com.fabio.eagleeyes.funcionalidades.monitoramento.activity.BloqueioActivity
import com.fabio.eagleeyes.global.FirebaseConfig

class BloqueioAppAccessibilityService : AccessibilityService() {

    private var syncManager: FirebaseSyncManager? = null
    private var ruleEvaluator: UsageRuleEvaluator? = null
    private var alertManager: AlertManager? = null
    private val TAG = "EagleEyesMonitor"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Serviço de Acessibilidade Conectado")
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        val user = FirebaseConfig.getAuth().currentUser
        val emailFilho = user?.email

        if (emailFilho == null) {
            Log.e(TAG, "Monitoramento não iniciado: Usuário não logado no Firebase.")
            // Tenta recuperar se houver delay no Auth
            return
        }

        if (syncManager == null) {
            syncManager = FirebaseSyncManager(emailFilho)
            syncManager?.startSync()
        }
        
        if (ruleEvaluator == null) {
            ruleEvaluator = UsageRuleEvaluator(this)
        }
        
        if (alertManager == null) {
            alertManager = AlertManager(AlertaRepositorio())
        }

        Log.d(TAG, "Componentes inicializados para $emailFilho")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Se os componentes não foram inicializados (ex: login demorou), tenta novamente
        if (syncManager == null) {
            inicializarComponentes()
        }

        val sync = syncManager ?: return
        val evaluator = ruleEvaluator ?: return
        val packageName = event.packageName?.toString() ?: return

        // Ignorar o próprio app para evitar loops de bloqueio
        if (packageName == this.packageName || packageName.contains("fabio.eagleyes")) return

        // 1. Proteção contra Burla (Desinstalação, Limpar Dados, Forçar Parada)
        // Isso deve rodar SEMPRE que houver interação em apps sensíveis
        if (verificarTentativaBurla(event, sync)) {
            Log.w(TAG, "Interação proibida detectada em: $packageName")
            executarBloqueio(packageName, "Tentativa de burla/desinstalação")
            return
        }

        // 2. Filtro de Eventos de Mudança de Estado (Abertura de Apps)
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (evaluator.shouldBlock(packageName, sync.regrasAtivas)) {
                Log.w(TAG, "App bloqueado por regra de uso: $packageName")
                executarBloqueio(packageName)
            }
        }
    }

    private fun verificarTentativaBurla(event: AccessibilityEvent, sync: FirebaseSyncManager): Boolean {
        val packageName = event.packageName?.toString() ?: ""
        
        // Verifica se estamos nas Configurações, Play Store ou Instalador de Pacotes
        val isConfigApp = packageName.contains("settings", ignoreCase = true) ||
                          packageName.contains("packageinstaller", ignoreCase = true) ||
                          packageName.contains("vending", ignoreCase = true) || // Play Store
                          sync.nomesApp.any { packageName.contains(it, ignoreCase = true) }

        if (!isConfigApp) return false

        // Analisa o nó atual em busca de termos sensíveis
        val source = event.source ?: rootInActiveWindow ?: return false
        
        // Termos padrões de segurança caso o Firebase ainda não tenha carregado
        val termosPadrao = listOf("desinstalar", "uninstall", "limpar dados", "clear data", "forçar parada", "force stop", "desativar")
        val termosParaVerificar = if (sync.termosBloqueio.isNotEmpty()) sync.termosBloqueio else termosPadrao

        return scanForForbiddenTerms(source, termosParaVerificar)
    }

    private fun scanForForbiddenTerms(node: AccessibilityNodeInfo, termos: List<String>): Boolean {
        for (termo in termos) {
            if (termo.length < 3) continue // Evita falsos positivos com termos curtos demais
            
            val foundNodes = node.findAccessibilityNodeInfosByText(termo)
            if (!foundNodes.isNullOrEmpty()) {
                for (foundNode in foundNodes) {
                    val text = foundNode.text?.toString()?.lowercase() ?: ""
                    val content = foundNode.contentDescription?.toString()?.lowercase() ?: ""
                    val termoLower = termo.lowercase()
                    
                    // Verifica se o texto é EXATAMENTE o termo ou contém ele de forma isolada
                    if (text.contains(termoLower) || content.contains(termoLower)) {
                        return true
                    }
                }
            }
        }
        
        // Busca recursiva manual se necessário (findAccessibilityNodeInfosByText geralmente é suficiente)
        return false
    }

    private fun executarBloqueio(packageName: String, motivo: String? = null) {
        val appName = getAppName(packageName)

        // 1. Volta para a Home imediatamente
        performGlobalAction(GLOBAL_ACTION_HOME)

        // 2. Notifica o Responsável
        alertManager?.enviarAlertaBloqueio(packageName, motivo ?: appName)

        // 3. Abre a tela de bloqueio
        val intent = Intent(this, BloqueioActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("APP_BLOQUEADO", motivo ?: appName)
        }
        startActivity(intent)
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) { packageName }
    }

    override fun onInterrupt() {}
}
