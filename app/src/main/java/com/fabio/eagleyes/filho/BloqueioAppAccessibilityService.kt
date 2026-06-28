package com.fabio.eagleyes.filho

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.fabio.eagleyes.alerta.AlertaRepositorio
import com.fabio.eagleyes.global.FirebaseConfig

class BloqueioAppAccessibilityService : AccessibilityService() {

    private lateinit var syncManager: FirebaseSyncManager
    private lateinit var ruleEvaluator: UsageRuleEvaluator
    private lateinit var securityPolicy: SecurityPolicyManager
    private lateinit var alertManager: AlertManager

    override fun onCreate() {
        super.onCreate()
        val user = FirebaseConfig.getAuth().currentUser
        val emailFilho = user?.email ?: return

        // Inicializa os gerenciadores especialistas
        syncManager = FirebaseSyncManager(emailFilho)
        ruleEvaluator = UsageRuleEvaluator(this)
        securityPolicy = SecurityPolicyManager()
        
        // Inicializa o AlertManager usando o Repositório existente
        val repositorio = AlertaRepositorio()
        alertManager = AlertManager(repositorio)

        // Inicia a sincronização com Firebase
        syncManager.startSync()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return

        if (packageName.contains("fabio.eagleyes") || packageName == this.packageName) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            // 1. Verificação de Segurança (Bypass nas Configurações)
            if (packageName == "com.android.settings") {
                if (securityPolicy.isAttemptingToBypass(
                        rootInActiveWindow, 
                        syncManager.termosBloqueio, 
                        syncManager.nomesApp
                    )) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    exibirTelaDeBloqueio("com.android.settings")
                    alertManager.enviarAlertaBypass()
                    return
                }
            }

            // 2. Verificação de Regras de Uso
            if (ruleEvaluator.shouldBlock(packageName, syncManager.regrasAtivas)) {
                alertManager.enviarAlertaBloqueio(packageName, getAppName(packageName))
                exibirTelaDeBloqueio(packageName)
            }
        }
    }

    private fun getAppName(packageName: String): String {
        if (packageName == "com.android.settings") return "Segurança"
        return try {
            val pm = packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) { packageName }
    }

    private fun exibirTelaDeBloqueio(packageName: String) {
        val intent = Intent(this, BloqueioActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("APP_BLOQUEADO", getAppName(packageName))
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}