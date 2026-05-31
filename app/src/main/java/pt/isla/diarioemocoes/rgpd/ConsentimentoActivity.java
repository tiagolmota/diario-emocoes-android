package pt.isla.diarioemocoes.rgpd;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pt.isla.diarioemocoes.R;
import pt.isla.diarioemocoes.ui.MainActivity;

/**
 * CONSENTIMENTO RGPD: ConsentimentoActivity
 *
 * Artigo 7.º do RGPD (Regulamento UE 2016/679): o consentimento deve ser
 * "livre, específico, informado e inequívoco". Esta Activity implementa
 * todos os requisitos normativos:
 *
 * - Informação clara sobre que dados são tratados e para quê (Art. 13.º)
 * - Consentimento activo por checkbox (não pré-marcada — Art. 7.º §2)
 * - Botão de aceitação inactivo até consentimento explícito
 * - Direito de acesso, rectificação e eliminação disponíveis na app (Art. 15.º-17.º)
 * - Dados armazenados apenas localmente no dispositivo — minimização (Art. 5.º §1.c)
 *
 * Categoria especial: dados de saúde/emocionais enquadram-se no Art. 9.º RGPD
 * (categorias especiais de dados). O tratamento é lícito com base no
 * consentimento explícito do titular (Art. 9.º §2.a).
 */
public class ConsentimentoActivity extends AppCompatActivity {

    private CheckBox checkBoxConsentimento;
    private CheckBox checkBoxMaiores;
    private Button buttonAceitar;
    private Button buttonRecusar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consentimento);

        checkBoxConsentimento = findViewById(R.id.checkBoxConsentimento);
        checkBoxMaiores       = findViewById(R.id.checkBoxMaiores);
        buttonAceitar         = findViewById(R.id.buttonAceitar);
        buttonRecusar         = findViewById(R.id.buttonRecusar);

        // Passo 1: Botão desactivado até ambas as caixas estarem marcadas
        // RGPD Art. 7.º — consentimento activo, nunca pré-marcado
        buttonAceitar.setEnabled(false);

        // Passo 2: Activar o botão apenas quando AMBAS as condições estão aceites
        View.OnClickListener verificarConsentimento = v -> {
            boolean ambosAceites = checkBoxConsentimento.isChecked()
                    && checkBoxMaiores.isChecked();
            buttonAceitar.setEnabled(ambosAceites);
        };
        checkBoxConsentimento.setOnClickListener(verificarConsentimento);
        checkBoxMaiores.setOnClickListener(verificarConsentimento);

        // Passo 3: Aceitar — registar consentimento com timestamp e avançar
        buttonAceitar.setOnClickListener(v -> {
            GestorConsentimento.getInstance(this).registarConsentimento();
            irParaMainActivity();
        });

        // Passo 4: Recusar — fechar a app sem guardar dados
        // RGPD Art. 7.º §3 — o titular pode retirar o consentimento a qualquer momento
        buttonRecusar.setOnClickListener(v -> finishAffinity());
    }

    /**
     * Passo 5: Navegar para MainActivity após consentimento registado.
     * FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK garante que
     * o utilizador não pode voltar ao ecrã de consentimento com "Voltar".
     */
    private void irParaMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Passo 6: Bloquear o botão "Voltar" — o utilizador deve fazer uma escolha
     * explícita (aceitar ou recusar). Não pode contornar o ecrã de consentimento.
     */
    @Override
    public void onBackPressed() {
        // Intencionalmente vazio — RGPD exige decisão explícita
        // O utilizador deve tocar em "Aceitar" ou "Recusar"
    }
}
