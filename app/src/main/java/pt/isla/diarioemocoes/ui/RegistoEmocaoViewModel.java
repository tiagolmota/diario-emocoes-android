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

/**
 * VIEWMODEL: RegistoEmocaoViewModel
 * Actualizado com apagarTodosOsRegistos() e totalRegistos para RGPD.
 */
public class RegistoEmocaoViewModel extends AndroidViewModel {

    private final RegistoEmocaoRepository repository;
    public final LiveData<List<RegistoEmocao>> todosOsRegistos;
    public final LiveData<Integer> totalRegistos;

    public RegistoEmocaoViewModel(@NonNull Application application) {
        super(application);
        repository      = new RegistoEmocaoRepository(application);
        todosOsRegistos = repository.getTodosOsRegistos();
        totalRegistos   = repository.getTotalRegistos();
    }

    /**
     * Guardar registo com validação integrada no Repository.
     * Retorna true se guardou, false se validação falhou.
     */
    public boolean guardarRegisto(String estadoEmocional, String notas, double temperatura) {
        long timestampAtual = System.currentTimeMillis();
        String dataFormatada = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestampAtual));

        RegistoEmocao novoRegisto = new RegistoEmocao(
                timestampAtual, dataFormatada, estadoEmocional, temperatura, notas
        );
        return repository.inserir(novoRegisto);
    }

    public void apagarRegisto(long id) {
        repository.apagar(id);
    }

    /**
     * RGPD Art. 17.º — Direito de Eliminação.
     * Chamado pela PrivacidadeActivity e disponível via Action Bar.
     */
    public void apagarTodosOsRegistos() {
        repository.apagarTudo();
    }
}
