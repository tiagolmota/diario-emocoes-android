package pt.isla.diarioemocoes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import pt.isla.diarioemocoes.R;

/**
 * SPLASH SCREEN: SplashActivity
 *
 * Passo 1: Requisito obrigatório do enunciado do Prof. Marco Tereso.
 * O Splash Screen cumpre duas funções distintas:
 *
 * Função técnica: Dar tempo ao sistema para inicializar recursos pesados
 * (ex: verificação de sessão Firebase, pré-carregamento de dados).
 * Nesta fase do projeto, serve como placeholder para futuras inicializações.
 *
 * Função de UX: Apresentar a identidade visual da app durante o arranque,
 * transmitindo a perceção de fluidez ao utilizador.
 *
 * A Activity é declarada no AndroidManifest.xml como LAUNCHER, substituindo
 * a MainActivity nesse papel — a MainActivity passa a ser iniciada
 * programaticamente a partir daqui.
 */
public class SplashActivity extends AppCompatActivity {

    // Passo 2: Duração do Splash em milissegundos.
    // 2000ms (2 segundos) é o valor recomendado pelas Material Design Guidelines.
    // Valores acima de 3000ms degradam a perceção de performance da aplicação.
    private static final int SPLASH_DURATION_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Passo 3: Inflação do layout do Splash — um ecrã simples com logo e nome da app.
        setContentView(R.layout.activity_splash);

        /**
         * Passo 4: Handler com postDelayed para transição temporizada.
         * 'new Handler(Looper.getMainLooper())' executa o Runnable na Main Thread
         * após o delay definido — sem bloquear o ecrã, sem thread separada necessária.
         *
         * NOTA: Esta abordagem com Handler é o padrão Java clássico ensinado em aula.
         * A alternativa moderna seria a SplashScreen API (Android 12+), mas aqui
         * mantemos compatibilidade retroativa com versões anteriores do Android.
         */
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Passo 5: Iniciar a MainActivity após o delay.
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // Passo 6: Fechar o Splash para que o botão "Voltar" não o reabra.
            finish();
        }, SPLASH_DURATION_MS);
    }
}
