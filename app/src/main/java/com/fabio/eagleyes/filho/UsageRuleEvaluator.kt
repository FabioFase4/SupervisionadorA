package com.fabio.eagleyes.filho

import android.app.usage.UsageStatsManager
import android.content.Context
import com.fabio.eagleyes.funcionalidades.regra.Regra
import java.util.*

class UsageRuleEvaluator(private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun shouldBlock(packageName: String, regras: List<Regra>): Boolean {
        // NUNCA bloqueia o Launcher ou Sistema básico
        if (packageName.contains("launcher") || packageName == "android" || packageName == "com.google.android.googlequicksearchbox") {
            return false
        }

        val nomeApp = getAppName(packageName)
        
        for (regra in regras) {
            if (regra.nomeApp.equals(nomeApp, ignoreCase = true) ||
                regra.nomeApp.equals(packageName, ignoreCase = true) ||
                regra.nomeApp == "Geral") {

                when (regra.tipo) {
                    "BLOQUEIO" -> return true
                    "HORARIO" -> {
                        val calendar = Calendar.getInstance()
                        val tempoAtualMinutos = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
                        val partes = regra.valor.split(":")
                        val tempoLimiteMinutos = (partes[0].toIntOrNull() ?: 0) * 60 + (if (partes.size > 1) partes[1].toIntOrNull() ?: 0 else 0)
                        if (tempoAtualMinutos >= tempoLimiteMinutos) return true
                    }
                    "LIMITE_TEMPO" -> {
                        val limiteMinutos = regra.valor.toLongOrNull() ?: 0
                        val tempoGastoMs = getTempoUsoHoje(packageName)
                        if ((tempoGastoMs / 60000) >= limiteMinutos) return true
                    }
                }
            }
        }
        return false
    }

    private fun getTempoUsoHoje(packageName: String): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis())
        return stats?.find { it.packageName == packageName }?.totalTimeInForeground ?: 0
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) { packageName }
    }
}
