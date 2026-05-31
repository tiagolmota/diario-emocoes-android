package pt.isla.diarioemocoes.rgpd;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * GESTOR DE CONSENTIMENTO RGPD: GestorConsentimento
 *
 * Responsabilidade única: gerir o estado de consentimento do utilizador
 * em conformidade com o Regulamento (UE) 2016/679 (RGPD).
 *
 * Implementa o requisito de AUDITABILIDADE do consentimento (Art. 7.º §1):
 * "O responsável pelo tratamento deve poder demonstrar que o titular dos dados
 * deu o seu consentimento para o tratamento dos seus dados pessoais."
 *
 * SEGURANÇA: as preferências são cifradas com AES-256-GCM via
 * EncryptedSharedPreferences (Jetpack Security) — os dados de consentimento
 * não são legíveis mesmo com acesso root ao dispositivo.
 *
 * Padrão Singleton: instância única partilhada por toda a aplicação.
 */
public class GestorConsentimento {

    // Chaves das preferências — prefixo "rgpd_" para identificação clara
    private static final String PREFS_NAME        = "rgpd_consentimento_seguro";
    private static final String KEY_CONSENTIU      = "rgpd_consentiu";
    private static final String KEY_DATA_CONSENTIMENTO = "rgpd_data_consentimento";
    private static final String KEY_VERSAO_POLITICA    = "rgpd_versao_politica";

    // Versão atual da política de privacidade — incrementar quando mudar
    private static final int VERSAO_POLITICA_ATUAL = 1;

    private static GestorConsentimento instance;
    private final SharedPreferences prefs;

    /**
     * Passo 1: Construtor privado — inicializa EncryptedSharedPreferences.
     * Fallback para SharedPreferences normal se a cifra falhar
     * (ex: dispositivos muito antigos sem suporte a Keystore).
     */
    private GestorConsentimento(Context context) {
        SharedPreferences tempPrefs;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            tempPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback seguro — preferências não cifradas em dispositivos incompatíveis
            tempPrefs = context.getSharedPreferences(PREFS_NAME + "_plain", Context.MODE_PRIVATE);
        }
        this.prefs = tempPrefs;
    }

    /**
     * Passo 2: Acesso à instância Singleton thread-safe.
     */
    public static synchronized GestorConsentimento getInstance(Context context) {
        if (instance == null) {
            instance = new GestorConsentimento(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Passo 3: Verificar se o consentimento já foi dado E se é para a versão atual da política.
     * RGPD Art. 7.º — se a política mudar, novo consentimento é obrigatório.
     */
    public boolean temConsentimentoValido() {
        boolean consentiu = prefs.getBoolean(KEY_CONSENTIU, false);
        int versaoRegistada = prefs.getInt(KEY_VERSAO_POLITICA, 0);
        return consentiu && (versaoRegistada >= VERSAO_POLITICA_ATUAL);
    }

    /**
     * Passo 4: Registar consentimento com timestamp auditável.
     * RGPD Art. 7.º §1 — o responsável deve poder demonstrar o consentimento.
     * O timestamp serve como prova do momento exacto da decisão.
     */
    public void registarConsentimento() {
        String dataAtual = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()
        ).format(new Date());

        prefs.edit()
                .putBoolean(KEY_CONSENTIU, true)
                .putString(KEY_DATA_CONSENTIMENTO, dataAtual)
                .putInt(KEY_VERSAO_POLITICA, VERSAO_POLITICA_ATUAL)
                .apply();
    }

    /**
     * Passo 5: DIREITO DE RETIRADA DO CONSENTIMENTO — Art. 7.º §3 RGPD.
     * "O titular tem o direito de retirar o seu consentimento a qualquer momento."
     * Apaga todos os dados pessoais armazenados (implementado em conjunto
     * com AppDatabase.apagar() na PrivacidadeActivity).
     */
    public void retirarConsentimento() {
        prefs.edit()
                .putBoolean(KEY_CONSENTIU, false)
                .remove(KEY_DATA_CONSENTIMENTO)
                .remove(KEY_VERSAO_POLITICA)
                .apply();
    }

    /**
     * Passo 6: Obter data de consentimento para exibição no painel de privacidade.
     * RGPD Art. 15.º — direito de acesso à informação sobre o tratamento.
     */
    public String getDataConsentimento() {
        return prefs.getString(KEY_DATA_CONSENTIMENTO, "Não registado");
    }
}
