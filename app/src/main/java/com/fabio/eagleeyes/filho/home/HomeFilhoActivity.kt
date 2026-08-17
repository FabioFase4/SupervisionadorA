package com.fabio.eagleeyes.filho.home

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager

import android.content.ComponentName
import android.content.Context
import android.content.Intent

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process

import android.provider.Settings

import android.text.TextUtils

import android.view.View

import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider

import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.alerta.activitiy.AlertasFilhoActivity
import com.fabio.eagleeyes.login.LoginActivity
import com.fabio.eagleeyes.funcionalidades.historico.RelatorioFilhoActivity
import com.fabio.eagleeyes.funcionalidades.monitoramento.BloqueioAppAccessibilityService
import com.fabio.eagleeyes.funcionalidades.monitoramento.MonitoramentoService
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.global.PolicyManagerReceiver
import com.fabio.eagleeyes.repositorio.UsoRepositorio

class HomeFilhoActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeFilhoViewModel
    private lateinit var txtRespostaIA: TextView
    private lateinit var txtNome: TextView
    private lateinit var txtEmail: TextView
    private lateinit var cardRespostaIA: CardView
    private lateinit var progressBar: ProgressBar

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName


    private var btnAtivarAdmin: Button? = null
    private var btnVerHistorico: Button? = null
    private var btnVerAlertasFilho: Button? = null
    private var btnAnaliseIA: Button? = null
    private var btnSair: Button? = null

    companion object {
        private const val ACTIVATION_REQUEST_CODE = 101
        private const val OVERLAY_REQUEST_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_filho)

        val usoRepositorio = UsoRepositorio(this)
        val factory = HomeFilhoViewModelFactory(usoRepositorio)
        viewModel = ViewModelProvider(this, factory).get(HomeFilhoViewModel::class.java)

        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, PolicyManagerReceiver::class.java)

        vincularViews()
        configurarObservers()
        configurarCliques()

        if (temPermissaoUso(this)) {
            iniciarServicoMonitoramento()
        }
    }

    override fun onResume() {
        super.onResume()
        verificarNecessidadeDeConfiguracao()
    }

    private fun vincularViews() {
        txtNome = findViewById(R.id.txtNomeDetalhes)
        txtEmail = findViewById(R.id.txtEmailDetalhes)
        txtRespostaIA = findViewById(R.id.txtRespostaIA)
        cardRespostaIA = findViewById(R.id.cardRespostaIA)

        btnVerHistorico = findViewById<Button>(R.id.btnVerHistorico)
        btnVerAlertasFilho = findViewById<Button>(R.id.btnVerAlertasFilho)

        btnAtivarAdmin = findViewById<Button>(R.id.btnAtivarAdmin)
        btnAnaliseIA = findViewById<Button>(R.id.btnAnaliseIA)
        btnSair = findViewById<Button>(R.id.btnSair)

        progressBar = findViewById(R.id.progressBar)
    }

    private fun configurarObservers() {
        viewModel.userName.observe(this) { nome ->
            txtNome.text = "Olá, $nome"
        }

        viewModel.userEmail.observe(this) { email ->
            txtEmail.text = email
        }

        viewModel.respostaIA.observe(this) { resposta ->
            if (resposta.isNotEmpty()) {
                cardRespostaIA.visibility = View.VISIBLE
                txtRespostaIA.text = resposta
            }
        }

        viewModel.carregando.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            if (isLoading) {
                cardRespostaIA.visibility = View.VISIBLE
                txtRespostaIA.text = "Analisando seu uso de tela..."
            }
            // Removido o cardRespostaIA.visibility = View.GONE para não esconder a resposta
        }

        // NOVO: Observer para erros (Essencial para saber se o Gemini falhou)
        viewModel.erro.observe(this) { mensagem ->
            if (!mensagem.isNullOrEmpty()) {
                cardRespostaIA.visibility = View.VISIBLE
                txtRespostaIA.text = mensagem
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configurarCliques() {
        btnVerHistorico?.setOnClickListener {
            val user = FirebaseConfig.getAuth().currentUser
            val intent = Intent(this, RelatorioFilhoActivity::class.java).apply {
                putExtra("FILHO_UID", user?.uid)
                putExtra("FILHO_NOME", viewModel.userName.value)
            }
            startActivity(intent)
        }

        btnVerAlertasFilho?.setOnClickListener {
            val user = FirebaseConfig.getAuth().currentUser
            val intent = Intent(this, AlertasFilhoActivity::class.java).apply {
                putExtra("FILHO_UID", user?.uid)
                putExtra("FILHO_NOME", viewModel.userName.value)
            }
            startActivity(intent)
        }

        btnAnaliseIA?.setOnClickListener {
            if (temPermissaoUso(this)) {
                viewModel.realizarAnaliseIA()
            } else {
                Toast.makeText(this, "Permissão de acesso ao uso necessária.", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }

        btnAtivarAdmin?.setOnClickListener {
            ativarProtecaoExtra()
        }

        btnSair?.setOnClickListener {
            stopService(Intent(this, MonitoramentoService::class.java))
            FirebaseConfig.getAuth().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun ativarProtecaoExtra() {
        // 1. Administrador do Dispositivo (Evita desinstalação)
        if (!devicePolicyManager.isAdminActive(compName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_description))
            }
            startActivityForResult(intent, ACTIVATION_REQUEST_CODE)
            return
        }

        // 2. Sobreposição (Necessário para a tela de bloqueio aparecer)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Ative 'Sobrepor a outros apps' para o EagleEyes", Toast.LENGTH_LONG).show()
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, OVERLAY_REQUEST_CODE)
            return
        }

        // 3. Serviço de Acessibilidade (Onde ocorre o bloqueio real)
        if (!isAccessibilityServiceEnabled(this, BloqueioAppAccessibilityService::class.java)) {
            solicitarAcessibilidade()
        } else {
            Toast.makeText(this, "Toda a proteção já está ativa!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun solicitarAcessibilidade() {
        Toast.makeText(this, "Ative o 'EagleEyes Bloqueio' na lista de Acessibilidade.", Toast.LENGTH_LONG).show()
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun verificarNecessidadeDeConfiguracao() {
        val adminAtivo = devicePolicyManager.isAdminActive(compName)
        val overlayAtivo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true
        val acessibilidadeAtiva = isAccessibilityServiceEnabled(this, BloqueioAppAccessibilityService::class.java)

        val btnAdmin = findViewById<Button>(R.id.btnAtivarAdmin)
        if (adminAtivo && overlayAtivo && acessibilidadeAtiva) {
            btnAdmin.text = "Proteção Ativa"
            btnAdmin.alpha = 0.6f
        } else {
            btnAdmin.text = "Concluir Configuração"
            btnAdmin.alpha = 1.0f
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ACTIVATION_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Admin ativado! Seguindo para Sobreposição...", Toast.LENGTH_SHORT).show()
                    ativarProtecaoExtra()
                }
            }
            OVERLAY_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Sobreposição ativada! Agora ative a Acessibilidade.", Toast.LENGTH_SHORT).show()
                    ativarProtecaoExtra()
                }
            }
        }
    }

    private fun iniciarServicoMonitoramento() {
        val intent = Intent(this, MonitoramentoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun temPermissaoUso(context: Context): Boolean {
        val appOs = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOs.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, service)
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) return true
        }
        return false
    }
}
