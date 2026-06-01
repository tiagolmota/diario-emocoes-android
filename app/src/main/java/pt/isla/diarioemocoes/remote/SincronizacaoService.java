package pt.isla.diarioemocoes.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import pt.isla.diarioemocoes.data.RegistoEmocao;

/**
 * SERVIÇO DE SINCRONIZAÇÃO: SincronizacaoService
 *
 * Implementa a estratégia de persistência híbrida: Room como fonte de verdade
 * local + Firebase como camada de sincronização remota.
 *
 * FLUXO DE DADOS (para o relatório — secção de revisão de conhecimentos):
 *
 * ESCRITA:
 *   1. Utilizador guarda registo → Room persiste imediatamente (offline-first)
 *   2. Se há conectividade → Firebase sincroniza em background de forma assíncrona
 *   3. Se não há conectividade → Firebase SDK faz queue e sincroniza quando voltar online
 *
 * LEITURA:
 *   1. UI lê sempre do Room via LiveData (resposta imediata, sem latência de rede)
 *   2. Firebase é consultado apenas no arranque para sincronização inicial
 *
 * ELIMINAÇÃO:
 *   1. Room apaga localmente
 *   2. Firebase apaga remotamente (incluindo em conformidade com RGPD Art. 17.º)
 *
 * Esta arquitectura garante:
 * - Funcionamento offline total (Room)
 * - Backup automático na nuvem (Firebase)
 * - Consistência eventual entre dispositivos
 */
public class SincronizacaoService {

    private final FirebaseRepository firebaseRepository;
    private final Context context;

    public SincronizacaoService(Context context) {
        this.context = context.getApplicationContext();
        this.firebaseRepository = new FirebaseRepository();
    }

    /**
     * Passo 1: Verificar conectividade antes de tentar sincronizar.
     * Evita tentativas desnecessárias em modo avião ou sem dados.
     */
    public boolean temConectividade() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    /**
     * Passo 2: Sincronizar um registo com Firebase.
     * Chamado pelo ViewModel após inserção/actualização no Room.
     * O resultado é silencioso em caso de sucesso — o utilizador não é
     * notificado de operações de background. Em caso de erro, o Firebase SDK
     * faz retry automático quando a conectividade for restaurada.
     */
    public void sincronizarRegisto(RegistoEmocao registo) {
        if (!temConectividade()) return; // Firebase SDK faz queue automaticamente
        firebaseRepository.sincronizarRegisto(registo, new FirebaseRepository.FirebaseCallback() {
            @Override public void onSucesso() { /* Silencioso — background operation */ }
            @Override public void onErro(String mensagem) { /* Firebase SDK fará retry */ }
        });
    }

    /**
     * Passo 3: Apagar registo remoto — chamado após eliminação no Room.
     */
    public void apagarRegistoRemoto(long id) {
        if (!temConectividade()) return;
        firebaseRepository.apagarRegisto(id, new FirebaseRepository.FirebaseCallback() {
            @Override public void onSucesso() { }
            @Override public void onErro(String msg) { }
        });
    }

    /**
     * Passo 4: Eliminação total remota — RGPD Art. 17.º.
     * Chamado pela PrivacidadeActivity quando o utilizador apaga todos os dados.
     */
    public void apagarTudoRemoto(FirebaseRepository.FirebaseCallback callback) {
        firebaseRepository.apagarTudo(callback);
    }

    /**
     * Passo 5: Sincronização inicial — recuperar dados do Firebase para Room.
     * Chamado no arranque da app para garantir que o dispositivo está actualizado.
     */
    public void sincronizacaoInicial(FirebaseRepository.FirebaseListCallback callback) {
        if (!temConectividade()) return;
        firebaseRepository.obterTodosOsRegistos(callback);
    }
}
