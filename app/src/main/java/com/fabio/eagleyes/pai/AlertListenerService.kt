package com.fabio.eagleyes.pai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fabio.eagleyes.alerta.Alerta
import com.fabio.eagleyes.alerta.AlertaDAO
import com.fabio.eagleyes.global.FirebaseConfig
import com.fabio.eagleyes.global.NotificationHelper
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class AlertListenerService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private val alertaDao = AlertaDAO()
    private val TAG = "EagleEyes_Service"
    
    // Tolerância de 24 horas para evitar problemas de relógio dessincronizado entre dispositivos
    private val startTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
    private val alertasProcessados = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        
        // 1. Inicia como Foreground Service para evitar que o Android mate o processo
        iniciarForeground()
        
        // 2. Notificação de Teste: Se esta aparecer, as permissões estão OK!
        notificationHelper.showNotification("EagleEyes Ativo", "Monitorando a segurança dos seus filhos.")
        
        // 3. Inicia a escuta no Firebase
        iniciarEscutaAlertas()
    }

    private fun iniciarForeground() {
        val channelId = "eagleeyes_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Serviço de Proteção", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Proteção EagleEyes")
            .setContentText("Escutando alertas em tempo real...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun iniciarEscutaAlertas() {
        val user = FirebaseConfig.getAuth().currentUser ?: return
        val emailPai = user.email ?: return
        val refPai = alertaDao.getAlertasRef(emailPai)

        Log.d(TAG, "Escutando caminho: ${refPai.path}")

        refPai.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "Nó de filho detectado: ${snapshot.key}")
                
                // Monitora alertas individuais dentro de cada filho
                snapshot.ref.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(alertSnap: DataSnapshot, prevName: String?) {
                        val alerta = alertSnap.getValue(Alerta::class.java)
                        val id = alertSnap.key ?: ""
                        
                        if (alerta != null) {
                            Log.d(TAG, "Alerta recebido: ${alerta.aplicativo} | ID: $id | Time: ${alerta.timestamp}")
                            
                            // Verifica se é novo e único
                            if (alerta.timestamp > startTime && !alertasProcessados.contains(id)) {
                                alertasProcessados.add(id)
                                notificationHelper.showNotification(
                                    "Novo Alerta: ${alerta.aplicativo}",
                                    alerta.mensagem
                                )
                            }
                        }
                    }
                    override fun onChildChanged(s: DataSnapshot, p: String?) {}
                    override fun onChildRemoved(s: DataSnapshot) {}
                    override fun onChildMoved(s: DataSnapshot, p: String?) {}
                    override fun onCancelled(e: DatabaseError) {}
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Erro Firebase: ${error.message}")
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
