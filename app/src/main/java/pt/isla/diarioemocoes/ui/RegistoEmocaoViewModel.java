package pt.isla.diarioemocoes.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pt.isla.diarioemocoes.data.RegistoEmocao;
import pt.isla.diarioemocoes.data.RegistoEmocaoRepository;
import pt.isla.diarioemocoes.remote.SincronizacaoService;

/**
 * VIEWMODEL: RegistoEmocaoViewModel
 *
 * Passo 1: Herda AndroidViewModel para acesso seguro ao ApplicationContext.
 * Passo 2: Orquestra CRUD local (Room) + sincronização remota (Firebase).
 * Passo 3: Expõe LiveData para observação reactiva pela MainActivity.
 */
public class RegistoEmocaoViewModel extends AndroidViewModel {

    private final RegistoEmocaoRepository repository;
    private final SincronizacaoService sincronizacaoService;

    public final LiveData<List<RegistoEmocao>> todosOsRegistos;
    public final LiveData<Integer> totalRegistos;

    public RegistoEmocaoViewModel(@NonNull Application application) {
        super(application);
        repository           = new RegistoEmocaoRepository(application);
        sincronizacaoService = new SincronizacaoService(application);
        todosOsRegistos      = repository.getTodosOsRegistos();
        totalRegistos        = repository.getTotalRegistos();
    }

    // =========================================================================
    // C — CREATE
    // =========================================================================
    /**
     * Passo 4: Guardar novo registo — Room primeiro, Firebase a seguir.
     * Retorna true se validação passou e Room persistiu, false caso contrário.
     */
    public boolean guardarRegisto(String estadoEmocional, String notas, double temperatura) {
        long timestampAtual = System.currentTimeMillis();
        String dataFormatada = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestampAtual));

        RegistoEmocao novoRegisto = new RegistoEmocao(
                timestampAtual, dataFormatada, estadoEmocional, temperatura, notas
        );

        boolean inserido = repository.inserir(novoRegisto);
        if (inserido) {
            // Sincronizar com Firebase em background — não bloqueia a UI
            sincronizacaoService.sincronizarRegisto(novoRegisto);
        }
        return inserido;
    }

    // =========================================================================
    // U — UPDATE
    // =========================================================================
    /**
     * Passo 5: Actualizar registo existente — Room + Firebase.
     */
    public boolean actualizarRegisto(RegistoEmocao registo) {
        boolean actualizado = repository.actualizar(registo);
        if (actualizado) {
            sincronizacaoService.sincronizarRegisto(registo);
        }
        return actualizado;
    }

    // =========================================================================
    // D — DELETE
    // =========================================================================
    /** Passo 6: Apagar registo único — Room + Firebase. */
    public void apagarRegisto(long id) {
        repository.apagar(id);
        sincronizacaoService.apagarRegistoRemoto(id);
    }

    /**
     * Passo 7: Apagar todos os dados — RGPD Art. 17.º.
     * Room local + Firebase remoto.
     */
    public void apagarTodosOsRegistos() {
        repository.apagarTudo();
        sincronizacaoService.apagarTudoRemoto(new pt.isla.diarioemocoes.remote.FirebaseRepository.FirebaseCallback() {
            @Override public void onSucesso() { }
            @Override public void onErro(String msg) { }
        });
    }
}
