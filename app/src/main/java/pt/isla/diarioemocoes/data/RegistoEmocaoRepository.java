package pt.isla.diarioemocoes.data;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REPOSITÓRIO: RegistoEmocaoRepository
 *
 * Passo 1: O padrão Repository é a "camada de mediação" entre o ViewModel
 * e as fontes de dados (Room local, Firebase remoto, API REST).
 * O ViewModel não conhece de onde vêm os dados — apenas consome o repositório.
 * Esta abstração é o que torna o código testável e escalável.
 *
 * DIFERENÇA JAVA vs KOTLIN:
 * Em Kotlin, as operações de escrita usam 'suspend' + coroutines.
 * Em Java, o mecanismo equivalente é um ExecutorService — um pool de threads
 * gerido pelo sistema operativo Android para operações de I/O em segundo plano.
 */
public class RegistoEmocaoRepository {

    private final RegistoEmocaoDao registoDao;
    private final LiveData<List<RegistoEmocao>> todosOsRegistos;

    /**
     * Passo 2: ExecutorService com thread única.
     * 'newSingleThreadExecutor()' garante que as escritas na base de dados
     * são serializadas (uma de cada vez), prevenindo conflitos de concorrência
     * sem a complexidade de um pool multi-thread.
     */
    private static final ExecutorService databaseWriteExecutor =
            Executors.newSingleThreadExecutor();

    /**
     * Passo 3: Construtor — inicializa o DAO a partir da AppDatabase Singleton.
     * Ao receber Application (e não Context de Activity), garantimos
     * que o repositório sobrevive a rotações de ecrã sem fugas de memória.
     */
    public RegistoEmocaoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        registoDao = db.registoDao();
        // O LiveData é obtido uma vez e o Room mantém-no atualizado automaticamente
        todosOsRegistos = registoDao.obterTodosOsRegistos();
    }

    /**
     * Passo 4: Exposição do LiveData para o ViewModel.
     * O ViewModel observa este objeto; qualquer alteração na tabela
     * propaga-se até à UI sem intervenção manual do programador.
     */
    public LiveData<List<RegistoEmocao>> getTodosOsRegistos() {
        return todosOsRegistos;
    }

    /**
     * Passo 5: Operação de INSERT em thread de background.
     * O lambda 'databaseWriteExecutor.execute(() -> ...)' executa o código
     * dentro dos parênteses numa thread separada — equivalente funcional
     * do 'viewModelScope.launch { }' do Kotlin.
     */
    public void inserir(RegistoEmocao registo) {
        databaseWriteExecutor.execute(() -> registoDao.inserirRegisto(registo));
    }

    /**
     * Passo 6: Operação de DELETE em thread de background.
     * Idêntico ao padrão de inserção — nunca bloqueamos a Main Thread
     * com operações de I/O, pois isso causaria ANR (App Not Responding).
     */
    public void apagar(long id) {
        databaseWriteExecutor.execute(() -> registoDao.apagarRegistoPorId(id));
    }
}
