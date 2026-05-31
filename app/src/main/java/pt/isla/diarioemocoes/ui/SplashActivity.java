package pt.isla.diarioemocoes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import pt.isla.diarioemocoes.rgpd.ConsentimentoActivity;
import pt.isla.diarioemocoes.rgpd.GestorConsentimento;

/**
 * SPLASH SCREEN: SplashActivity
 *
 * Actualizado com verificação de consentimento RGPD:
 * - Primeiro arranque → ConsentimentoActivity (obrigatório)
 * - Arranques subsequentes com consentimento válido → MainActivity
 * - Consentimento expirado (nova versão de política) → ConsentimentoActivity
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(pt.isla.diarioemocoes.R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            GestorConsentimento gestor = GestorConsentimento.getInstance(this);

            // Passo 1: Verificar consentimento RGPD antes de prosseguir
            // Se não houver consentimento válido → ecrã de consentimento obrigatório
            if (!gestor.temConsentimentoValido()) {
                startActivity(new Intent(this, ConsentimentoActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }, SPLASH_DURATION_MS);
    }
}
