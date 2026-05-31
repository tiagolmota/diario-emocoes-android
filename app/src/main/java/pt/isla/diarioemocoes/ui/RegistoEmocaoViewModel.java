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
 *
 * Passo 1: Herdamos de AndroidViewModel (e não de ViewModel simples).
 * A diferença é cirúrgica mas crítica: AndroidViewModel recebe 'Application'
 * no construtor, permitindo acesso ao ApplicationContext sem criar
 * um memory leak — o ApplicationContext existe enquanto o processo existir,
 * ao contrário do Context de uma Activity que é destruída na rotação do ecrã.
 *
 * O ViewModel sobrevive a mudanças de configuração (rotação, mudança de idioma).
 * Se a Activity for destruída e recriada, o ViewModel permanece intacto
 * com todos os dados já carregados — sem nova consulta à base de dados.
 */
public class RegistoEmocaoViewModel extends AndroidViewModel {

    private final RegistoEmocaoRepository repository;
    // Passo 2: O LiveData exposto à UI é final — a Activity não pode substituí-lo,
    // apenas observá-lo. Separação de responsabilidades em ação.
    public final LiveData<List<RegistoEmocao>> todosOsRegistos;

    /**
     * Passo 3: Construtor obrigatório com @NonNull.
     * A anotação @NonNull documenta explicitamente o contrato:
     * application nunca pode ser null — o sistema Android garante isso
     * quando instancia o ViewModel via ViewModelProvider.
     */
    public RegistoEmocaoViewModel(@NonNull Application application) {
        super(application);
        repository = new RegistoEmocaoRepository(application);
        todosOsRegistos = repository.getTodosOsRegistos();
    }

    /**
     * Passo 4: Método de inserção com geração automática do timestamp.
     * System.currentTimeMillis() gera a chave primária — único, crescente,
     * e indexável eficientemente pelo B-Tree do SQLite.
     *
     * SimpleDateFormat formata a data para leitura humana (exibição na UI)
     * enquanto o long raw serve de identificador e critério de ordenação.
     */
    public void guardarRegisto(String estadoEmocional, String notas, double temperatura) {
        long timestampAtual = System.currentTimeMillis();
        String dataFormatada = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestampAtual));

        RegistoEmocao novoRegisto = new RegistoEmocao(
                timestampAtual,
                dataFormatada,
                estadoEmocional,
                temperatura,
                notas
        );
        repository.inserir(novoRegisto);
    }

    /**
     * Passo 5: Método de eliminação — delega ao repositório que executa
     * a operação em background thread via ExecutorService.
     */
    public void apagarRegisto(long id) {
        repository.apagar(id);
    }
}
