package pt.isla.diarioemocoes.rgpd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import pt.isla.diarioemocoes.R;
import pt.isla.diarioemocoes.ui.RegistoEmocaoViewModel;

/**
 * PAINEL DE PRIVACIDADE: PrivacidadeActivity
 *
 * Implementa os direitos do titular previstos no RGPD (Cap. III):
 *
 * Art. 13.º — Informação: que dados, para quê, por quanto tempo
 * Art. 15.º — Acesso: o utilizador vê quando consentiu
 * Art. 17.º — Eliminação ("direito ao esquecimento"): apagar todos os dados
 * Art. 7.º §3 — Retirada do consentimento: terminar o tratamento
 *
 * Categoria especial de dados (Art. 9.º): estados emocionais são dados
 * de saúde psicológica — tratamento lícito apenas com consentimento explícito.
 */
public class PrivacidadeActivity extends AppCompatActivity {

    private RegistoEmocaoViewModel viewModel;
    private GestorConsentimento gestorConsentimento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacidade);

        viewModel = new ViewModelProvider(this).get(RegistoEmocaoViewModel.class);
        gestorConsentimento = GestorConsentimento.getInstance(this);

        // Passo 1: Mostrar data de consentimento (Art. 15.º — direito de acesso)
        TextView textViewDataConsentimento = findViewById(R.id.textViewDataConsentimento);
        textViewDataConsentimento.setText(
                getString(R.string.privacidade_data_consentimento,
                        gestorConsentimento.getDataConsentimento())
        );

        // Passo 2: DIREITO DE ELIMINAÇÃO — Art. 17.º RGPD ("Direito ao Esquecimento")
        Button buttonApagarTudo = findViewById(R.id.buttonApagarTodosDados);
        buttonApagarTudo.setOnClickListener(v -> mostrarDialogoEliminacao());

        // Passo 3: RETIRADA DE CONSENTIMENTO — Art. 7.º §3 RGPD
        Button buttonRetirarConsentimento = findViewById(R.id.buttonRetirarConsentimento);
        buttonRetirarConsentimento.setOnClickListener(v -> mostrarDialogoRetiradaConsentimento());
    }

    /**
     * Passo 4: Diálogo de confirmação para eliminação total de dados.
     * Dupla confirmação obrigatória — acção completamente irreversível.
     */
    private void mostrarDialogoEliminacao() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.privacidade_apagar_titulo)
                .setMessage(R.string.privacidade_apagar_mensagem)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.privacidade_apagar_confirmar, (d, w) -> {
                    // Segundo nível de confirmação — acção irreversível
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.privacidade_apagar_titulo2)
                            .setMessage(R.string.privacidade_apagar_mensagem2)
                            .setPositiveButton(R.string.privacidade_apagar_definitivo, (d2, w2) -> {
                                viewModel.apagarTodosOsRegistos();
                                Toast.makeText(this,
                                        R.string.privacidade_dados_eliminados,
                                        Toast.LENGTH_LONG).show();
                            })
                            .setNegativeButton(R.string.dialog_cancelar, null)
                            .show();
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }

    /**
     * Passo 5: Retirada de consentimento — apaga dados E consentimento,
     * e reinicia a app no ecrã de consentimento.
     * RGPD Art. 7.º §3: a retirada não afecta a licitude do tratamento anterior.
     */
    private void mostrarDialogoRetiradaConsentimento() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.privacidade_retirar_titulo)
                .setMessage(R.string.privacidade_retirar_mensagem)
                .setPositiveButton(R.string.privacidade_retirar_confirmar, (d, w) -> {
                    // Apagar todos os registos da base de dados
                    viewModel.apagarTodosOsRegistos();
                    // Apagar registo de consentimento
                    gestorConsentimento.retirarConsentimento();
                    Toast.makeText(this,
                            R.string.privacidade_consentimento_retirado,
                            Toast.LENGTH_LONG).show();
                    // Reiniciar app no ecrã de consentimento
                    Intent intent = new Intent(this, ConsentimentoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }
}
