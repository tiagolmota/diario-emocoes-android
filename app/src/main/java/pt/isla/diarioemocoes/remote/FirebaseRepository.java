package pt.isla.diarioemocoes.remote;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pt.isla.diarioemocoes.data.RegistoEmocao;

/**
 * REPOSITÓRIO REMOTO: FirebaseRepository
 *
 * Implementa a integração com o Firebase Realtime Database — base de dados
 * externa exigida pelo enunciado do Prof. Marco Tereso.
 *
 * JUSTIFICAÇÃO DA ESCOLHA (para o relatório):
 * O Firebase Realtime Database foi seleccionado em detrimento de uma API PHP/MySQL
 * por três razões arquitecturais:
 *
 * 1. SDK nativo Android: comunicação directa sem camada HTTP manual,
 *    eliminando o código boilerplate de HttpURLConnection e parsing JSON.
 *
 * 2. Sincronização em tempo real: o ValueEventListener propaga alterações
 *    imediatamente a todos os clientes ligados — adequado para um diário
 *    que possa ser acedido em múltiplos dispositivos.
 *
 * 3. Persistência offline: o Firebase SDK mantém uma cache local e sincroniza
 *    automaticamente quando a conectividade é restaurada, garantindo que a
 *    app funciona sem internet (offline-first).
 *
 * ESTRATÉGIA DE PERSISTÊNCIA HÍBRIDA:
 * Room SQLite = fonte de verdade local (leitura rápida, offline)
 * Firebase    = sincronização remota (backup, multi-dispositivo)
 * A escrita vai primeiro ao Room; a sincronização com Firebase é assíncrona.
 *
 * RGPD Art. 46.º — Transferências para países terceiros:
 * O Firebase (Google) opera sob o EU-US Data Privacy Framework.
 * Para conformidade total, configurar a região do servidor para europa-west1
 * nas definições do projecto Firebase.
 */
public class FirebaseRepository {

    // Nó raiz da base de dados onde os registos são armazenados
    private static final String NODE_REGISTOS = "diario_emocoes";

    private final DatabaseReference dbRef;

    /**
     * Passo 1: Interface de callback para comunicação assíncrona.
     * O Firebase opera em callbacks — este padrão evita bloquear a UI thread.
     */
    public interface FirebaseCallback {
        void onSucesso();
        void onErro(String mensagem);
    }

    public interface FirebaseListCallback {
        void onDados(List<RegistoEmocao> registos);
        void onErro(String mensagem);
    }

    /**
     * Passo 2: Construtor — inicializa referência ao nó da BD.
     * FirebaseDatabase.getInstance() usa o URL definido em google-services.json.
     * setPersistenceEnabled(true) activa cache offline automática.
     */
    public FirebaseRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Activar persistência offline — a app funciona sem internet
        database.setPersistenceEnabled(true);
        dbRef = database.getReference(NODE_REGISTOS);
    }

    // =========================================================================
    // C — CREATE / U — UPDATE (sincronização para Firebase)
    // =========================================================================

    /**
     * Passo 3: Enviar registo para Firebase (CREATE ou UPDATE).
     * setValue() sobrescreve o nó completo — funciona como upsert.
     * A chave do nó é o ID do registo (timestamp Long) convertido para String.
     *
     * Estrutura na Firebase:
     * diario_emocoes/
     *   └── "1748789400000"/
     *         ├── id: 1748789400000
     *         ├── dataHoraLegivel: "01/06/2026 14:30"
     *         ├── estadoEmocional: "Motivado"
     *         ├── temperaturaAmbiente: 22.5
     *         └── notasTexto: "Dia produtivo..."
     */
    public void sincronizarRegisto(RegistoEmocao registo, FirebaseCallback callback) {
        dbRef.child(String.valueOf(registo.getId()))
                .setValue(registo)
                .addOnSuccessListener(aVoid -> callback.onSucesso())
                .addOnFailureListener(e -> callback.onErro(e.getMessage()));
    }

    // =========================================================================
    // R — READ (leitura da Firebase)
    // =========================================================================

    /**
     * Passo 4: Ler todos os registos da Firebase uma única vez (one-time read).
     * Usado para sincronização inicial quando o utilizador instala a app
     * num novo dispositivo — recupera os dados do servidor.
     */
    public void obterTodosOsRegistos(FirebaseListCallback callback) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<RegistoEmocao> registos = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    RegistoEmocao registo = child.getValue(RegistoEmocao.class);
                    if (registo != null) {
                        registos.add(registo);
                    }
                }
                callback.onDados(registos);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onErro(error.getMessage());
            }
        });
    }

    // =========================================================================
    // D — DELETE (eliminação na Firebase)
    // =========================================================================

    /**
     * Passo 5: Apagar registo específico na Firebase.
     * removeValue() elimina o nó e todos os seus filhos.
     */
    public void apagarRegisto(long id, FirebaseCallback callback) {
        dbRef.child(String.valueOf(id))
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSucesso())
                .addOnFailureListener(e -> callback.onErro(e.getMessage()));
    }

    /**
     * Passo 6: Apagar todos os registos — RGPD Art. 17.º.
     * Remove o nó raiz completo, eliminando todos os dados do utilizador
     * do servidor Firebase.
     */
    public void apagarTudo(FirebaseCallback callback) {
        dbRef.removeValue()
                .addOnSuccessListener(aVoid -> callback.onSucesso())
                .addOnFailureListener(e -> callback.onErro(e.getMessage()));
    }
}
