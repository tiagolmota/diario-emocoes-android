package pt.isla.diarioemocoes.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * DAO: RegistoEmocaoDao
 *
 * Passo 1: A anotação @Dao marca esta interface como um Objeto de Acesso a Dados.
 * O compilador do Room gera automaticamente a implementação concreta em tempo
 * de compilação — o programador nunca escreve SQL "solto" em strings dispersas.
 *
 * DIFERENÇA JAVA vs KOTLIN:
 * Em Kotlin usámos Flow<List<RegistoEmocao>> para reatividade.
 * Em Java, a abordagem mais estável e amplamente ensinada é LiveData<List<RegistoEmocao>>.
 * Ambos são observáveis reativos; o LiveData está profundamente integrado
 * no ecossistema Java do Android sem necessitar de coroutines.
 *
 * As operações de escrita (INSERT, DELETE) são executadas em threads separadas
 * pelo Executor que definiremos no repositório — equivalente ao 'suspend' do Kotlin.
 */
@Dao
public interface RegistoEmocaoDao {

    /**
     * Passo 2: Operação CREATE/UPDATE (Upsert).
     * OnConflictStrategy.REPLACE: se um registo com o mesmo ID já existir,
     * o Room apaga-o e insere o novo — comportamento de "guardar com substituição".
     * Útil para edição de registos sem necessitar de uma query @Update separada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirRegisto(RegistoEmocao registo);

    /**
     * Passo 3: Operação READ com LiveData.
     * O Room executa esta query numa thread de I/O automaticamente.
     * O LiveData notifica qualquer Observer ativo (a Activity/Fragment)
     * sempre que os dados na tabela se alteram — zero polling, zero código manual.
     * ORDER BY id DESC garante listagem do mais recente para o mais antigo.
     */
    @Query("SELECT * FROM diario_emocoes ORDER BY id DESC")
    LiveData<List<RegistoEmocao>> obterTodosOsRegistos();

    /**
     * Passo 4: Operação DELETE parametrizada.
     * O parâmetro ':id' na query SQL é mapeado diretamente para o argumento 'id'
     * do método — o Room trata da sanitização para prevenir SQL injection.
     */
    @Query("DELETE FROM diario_emocoes WHERE id = :id")
    void apagarRegistoPorId(long id);
}
