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
 * Mediador entre ViewModel e fontes de dados.
 * CRUD completo: inserir, obter, actualizar, apagar.
 * Todas as operações de escrita executam em background thread via ExecutorService.
 */
public class RegistoEmocaoRepository {

    private final RegistoEmocaoDao registoDao;
    private final LiveData<List<RegistoEmocao>> todosOsRegistos;
    private final LiveData<Integer> totalRegistos;

    private static final ExecutorService databaseWriteExecutor =
            Executors.newSingleThreadExecutor();

    public RegistoEmocaoRepository(Application application) {
        AppDatabase db  = AppDatabase.getDatabase(application);
        registoDao      = db.registoDao();
        todosOsRegistos = registoDao.obterTodosOsRegistos();
        totalRegistos   = registoDao.contarRegistos();
    }

    // R — READ
    public LiveData<List<RegistoEmocao>> getTodosOsRegistos() { return todosOsRegistos; }
    public LiveData<Integer> getTotalRegistos()               { return totalRegistos; }
    public LiveData<RegistoEmocao> getRegistoPorId(long id)  { return registoDao.obterRegistoPorId(id); }

    // C — CREATE (com validação)
    public boolean inserir(RegistoEmocao registo) {
        String estadoSanitizado = ValidadorDados.sanitizar(registo.getEstadoEmocional());
        String notasSanitizadas = ValidadorDados.sanitizar(registo.getNotasTexto());
        if (!ValidadorDados.validarEstado(estadoSanitizado).valido) return false;
        if (!ValidadorDados.validarNotas(notasSanitizadas).valido)  return false;
        registo.setEstadoEmocional(estadoSanitizado);
        registo.setNotasTexto(notasSanitizadas);
        databaseWriteExecutor.execute(() -> registoDao.inserirRegisto(registo));
        return true;
    }

    // U — UPDATE (com validação)
    public boolean actualizar(RegistoEmocao registo) {
        String estadoSanitizado = ValidadorDados.sanitizar(registo.getEstadoEmocional());
        String notasSanitizadas = ValidadorDados.sanitizar(registo.getNotasTexto());
        if (!ValidadorDados.validarEstado(estadoSanitizado).valido) return false;
        if (!ValidadorDados.validarNotas(notasSanitizadas).valido)  return false;
        registo.setEstadoEmocional(estadoSanitizado);
        registo.setNotasTexto(notasSanitizadas);
        databaseWriteExecutor.execute(() -> registoDao.actualizarRegisto(registo));
        return true;
    }

    // D — DELETE
    public void apagar(long id)  { databaseWriteExecutor.execute(() -> registoDao.apagarRegistoPorId(id)); }
    public void apagarTudo()     { databaseWriteExecutor.execute(() -> registoDao.apagarTodosOsRegistos()); }
}
