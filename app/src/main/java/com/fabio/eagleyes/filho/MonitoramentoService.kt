package com.fabio.eagleyes.filho

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

import androidx.core.app.NotificationCompat

import com.fabio.eagleyes.R
import com.fabio.eagleyes.global.FirebaseConfig
import com.fabio.eagleyes.repositorios.UsoRepositorio

import kotlinx.coroutines.*

import java.text.SimpleDateFormat
import java.util.*

class MonitoramentoService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var usoRepositorio: UsoRepositorio
    private val TAG = "MonitoramentoService"

    override fun onCreate() {
        super.onCreate()
        usoRepositorio = UsoRepositorio(this)
        startForegroundService()
        iniciarLoopMonitoramento()
    }

    private fun startForegroundService() {
        val channelId = "monitoramento_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monitoramento EagleEyes",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("EagleEyes em execução")
            .setContentText("Protegendo este dispositivo...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Certifique-se que este ícone existe
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun iniciarLoopMonitoramento() {
        serviceScope.launch {
            while (isActive) {
                try {
                    sincronizarUsoComFirebase()
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao sincronizar uso: ${e.message}")
                }
                delay(10 * 60 * 1000) // Sincroniza a cada 10 minutos
            }
        }
    }

    private fun sincronizarUsoComFirebase() {
        val user = FirebaseConfig.getAuth().currentUser ?: return
        val emailFilho = user.email ?: return
        val encodedEmail = emailFilho.replace(".", ",")

        val dadosUso = usoRepositorio.buscarDadosUso()
        val dataAtual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val ref = FirebaseConfig.getFilhosRef()
            .child(encodedEmail)
            .child("historicoUso")
            .child(dataAtual)

        val mapaUso = mutableMapOf<String, Any>()
        for (par in dadosUso) {
            val packageLimpo = par.first.replace(".", "_")
            mapaUso[packageLimpo] = par.second // Tempo em milissegundos
        }

        if (mapaUso.isNotEmpty()) {
            ref.updateChildren(mapaUso)
                .addOnSuccessListener { Log.d(TAG, "Uso sincronizado com sucesso!") }
                .addOnFailureListener { e -> Log.e(TAG, "Falha ao sincronizar: ${e.message}") }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
