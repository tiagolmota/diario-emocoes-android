package pt.isla.diarioemocoes.data;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pt.isla.diarioemocoes.security.ValidadorDados;

/**
 * REPOSITÓRIO: RegistoEmocaoRepository
 *
 * Actualizado com:
 * - Validação de dados antes de persistir (OWASP + RGPD Art. 5.º §1.d)
 * - Sanitização de input (segurança)
 * - apagarTodosOsRegistos() para RGPD Art. 17.º
 * - contarRegistos() para painel de privacidade Art. 15.º
 */
public class RegistoEmocaoRepository {

    private final RegistoEmocaoDao registoDao;
    private final LiveData<List<RegistoEmocao>> todosOsRegistos;
    private final LiveData<Integer> totalRegistos;

    private static final ExecutorService databaseWriteExecutor =
            Executors.newSingleThreadExecutor();

    public RegistoEmocaoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        registoDao      = db.registoDao();
        todosOsRegistos = registoDao.obterTodosOsRegistos();
        totalRegistos   = registoDao.contarRegistos();
    }

    public LiveData<List<RegistoEmocao>> getTodosOsRegistos() {
        return todosOsRegistos;
    }

    public LiveData<Integer> getTotalRegistos() {
        return totalRegistos;
    }

    /**
     * Passo 1: Inserção com validação e sanitização prévia.
     * RGPD Art. 5.º §1.d — exactidão: só armazenar dados válidos.
     * Retorna false se a validação falhar, true se inseriu com sucesso.
     */
    public boolean inserir(RegistoEmocao registo) {
        // Sanitizar antes de validar
        String estadoSanitizado = ValidadorDados.sanitizar(registo.getEstadoEmocional());
        String notasSanitizadas = ValidadorDados.sanitizar(registo.getNotasTexto());

        ValidadorDados.ResultadoValidacao vEstado = ValidadorDados.validarEstado(estadoSanitizado);
        ValidadorDados.ResultadoValidacao vNotas  = ValidadorDados.validarNotas(notasSanitizadas);

        if (!vEstado.valido || !vNotas.valido) return false;

        // Actualizar com valores sanitizados antes de persistir
        registo.setEstadoEmocional(estadoSanitizado);
        registo.setNotasTexto(notasSanitizadas);

        databaseWriteExecutor.execute(() -> registoDao.inserirRegisto(registo));
        return true;
    }

    public void apagar(long id) {
        databaseWriteExecutor.execute(() -> registoDao.apagarRegistoPorId(id));
    }

    /**
     * Passo 2: Eliminação total — RGPD Art. 17.º.
     * Executado em background thread; a UI observa via LiveData.
     */
    public void apagarTudo() {
        databaseWriteExecutor.execute(() -> registoDao.apagarTodosOsRegistos());
    }
}
