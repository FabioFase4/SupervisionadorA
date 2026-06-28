package com.fabio.eagleyes.filho

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleyes.R
import com.fabio.eagleyes.alerta.AlertasFilhoActivity
import com.fabio.eagleyes.auth.activity.LoginActivity
import com.fabio.eagleyes.funcionalidades.analiseIA.AnaliseViewModel
import com.fabio.eagleyes.funcionalidades.historico.RelatorioFilho
import com.fabio.eagleyes.global.FirebaseConfig
import com.fabio.eagleyes.global.PolicyManagerReceiver
import java.util.Calendar

class HomeFilhoActivity : AppCompatActivity() {

    private lateinit var analiseVM: AnaliseViewModel
    private lateinit var txtRespostaIA: TextView
    private lateinit var txtNome: TextView
    private lateinit var txtEmail: TextView
    
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName
    private val ACTIVATION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_filho)

        // Inicialização do Device Admin
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, PolicyManagerReceiver::class.java)

        // Inicialização do ViewModel via ViewModelProvider
        analiseVM = ViewModelProvider(this).get(AnaliseViewModel::class.java)

        // Referências da UI
        txtNome = findViewById(R.id.txtNomeDetalhes)
        txtEmail = findViewById(R.id.txtEmailDetalhes)
        val btnVerHistorico = findViewById<Button>(R.id.btnVerHistorico)
        val btnVerAlertas = findViewById<Button>(R.id.btnVerAlertasFilho)
        val btnAnaliseIA = findViewById<Button>(R.id.btnAnaliseIA)
        val btnAtivarAdmin = findViewById<Button>(R.id.btnAtivarAdmin)

        txtRespostaIA = findViewById(R.id.txtRespostaIA)
        val btnSair = findViewById<Button>(R.id.btnSair)

        // BUSCA DADOS REAIS DO FIREBASE
        carregarDadosPerfil()

        // Inicia o serviço se já houver permissão
        if (temPermissao(this)) {
            iniciarServicoMonitoramento()
        }

        // Configura os observadores para a resposta da IA
        analiseVM.respostaIA.observe(this) { resposta ->
            txtRespostaIA.text = resposta
            txtRespostaIA.visibility = View.VISIBLE
        }

        analiseVM.carregando.observe(this) { isLoading ->
            if (isLoading) {
                txtRespostaIA.text = "Analisando dados de uso..."
                txtRespostaIA.visibility = View.VISIBLE
            }
        }

        // Navegações e Cliques
        btnVerHistorico.setOnClickListener {
            val user = FirebaseConfig.getAuth().currentUser
            val intent = Intent(this, RelatorioFilho::class.java)
            intent.putExtra("FILHO_EMAIL", user?.email)
            intent.putExtra("FILHO_NOME", txtNome.text.toString().replace("Olá, ", ""))
            startActivity(intent)
        }

        btnVerAlertas.setOnClickListener {
            val user = FirebaseConfig.getAuth().currentUser
            val intent = Intent(this, AlertasFilhoActivity::class.java)
            intent.putExtra("FILHO_EMAIL", user?.email)
            intent.putExtra("FILHO_NOME", txtNome.text.toString().replace("Olá, ", ""))
            startActivity(intent)
        }
        
        btnAnaliseIA.setOnClickListener {
            if (temPermissao(this)) {
                iniciarServicoMonitoramento()
                executarAnaliseIA()
            } else {
                Toast.makeText(this, "Permissão de acesso ao uso é necessária.", Toast.LENGTH_LONG).show()
                abrirConfiguracoesUso(this)
            }
        }

        btnAtivarAdmin.setOnClickListener {
            if (!devicePolicyManager.isAdminActive(compName)) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_description))
                }
                startActivityForResult(intent, ACTIVATION_REQUEST_CODE)
            } else {
                Toast.makeText(this, "A proteção extra já está ativada!", Toast.LENGTH_SHORT).show()
            }
        }

        btnSair.setOnClickListener {
            stopService(Intent(this, MonitoramentoService::class.java))
            FirebaseConfig.getAuth().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun carregarDadosPerfil() {
        val user = FirebaseConfig.getAuth().currentUser ?: return
        
        // Define e-mail (que já temos do Auth)
        txtEmail.text = user.email
        
        // Busca o Nome no Database usando o UID
        FirebaseConfig.getDatabase().child("usuarios").child("filhos").child(user.uid)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nome = snapshot.child("nome").getValue(String::class.java)
                    if (!nome.isNullOrEmpty()) {
                        txtNome.text = "Olá, $nome"
                    } else {
                        txtNome.text = "Olá, ${user.email?.substringBefore("@")}"
                    }
                }
            }.addOnFailureListener {
                txtNome.text = "Olá, ${user.email?.substringBefore("@")}"
            }
    }

    // ... (restante dos métodos permanecem iguais)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Proteção extra ativada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "A ativação é necessária para maior segurança.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarServicoMonitoramento() {
        val intent = Intent(this, MonitoramentoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun executarAnaliseIA() {
        val stats = verTempo(this)
        if (stats.isEmpty()) {
            txtRespostaIA.visibility = View.VISIBLE
            txtRespostaIA.text = "Nenhum dado de uso encontrado nas últimas 24h."
            return
        }

        var textoFinal = "Atividade Recente:\n"
        for (app in stats.take(8)) {
            val nomeSimples = app.first.substringAfterLast(".")
            val tempoApp = formatarTempo(app.second)
            textoFinal += "- $nomeSimples: $tempoApp\n"
        }

        val prompt = "Analise o tempo de uso de tela abaixo e dê uma recomendação motivadora de saúde digital de forma direta: \n$textoFinal"
        analiseVM.analisar(prompt)
    }

    private fun abrirConfiguracoesUso(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    private fun temPermissao(context: Context): Boolean {
        val appOs = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOs.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun verTempo(context: Context): List<Pair<String, Long>> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendario = Calendar.getInstance()
        val fimTempo = calendario.timeInMillis
        calendario.add(Calendar.DAY_OF_YEAR, -1)
        val inicioTempo = calendario.timeInMillis

        val status = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            inicioTempo,
            fimTempo
        )
        val result = mutableListOf<Pair<String, Long>>()
        status?.forEach {
            if (it.totalTimeInForeground > 0) {
                result.add(it.packageName to it.totalTimeInForeground)
            }
        }
        return result.sortedByDescending { it.second }
    }

    private fun formatarTempo(ms: Long): String {
        val minutos = ms / 1000 / 60
        val horas = minutos / 60
        return if (horas > 0) "${horas}h ${minutos % 60}m" else "${minutos}m"
    }
}