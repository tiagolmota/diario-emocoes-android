package pt.isla.diarioemocoes.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO: RegistoEmocaoDao
 *
 * CRUD completo e explícito — requisito obrigatório do enunciado DAM:
 * C — @Insert  → inserirRegisto()
 * R — @Query   → obterTodosOsRegistos(), obterRegistoPorId()
 * U — @Update  → actualizarRegisto()
 * D — @Query   → apagarRegistoPorId(), apagarTodosOsRegistos()
 */
@Dao
public interface RegistoEmocaoDao {

    // -------------------------------------------------------------------------
    // C — CREATE
    // -------------------------------------------------------------------------
    /**
     * Passo 1: Inserção. OnConflictStrategy.ABORT garante que não substitui
     * silenciosamente — se o ID existir, lança excepção (comportamento explícito).
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void inserirRegisto(RegistoEmocao registo);

    // -------------------------------------------------------------------------
    // R — READ
    // -------------------------------------------------------------------------
    /** Passo 2: Leitura reactiva de todos os registos (mais recente primeiro). */
    @Query("SELECT * FROM diario_emocoes ORDER BY id DESC")
    LiveData<List<RegistoEmocao>> obterTodosOsRegistos();

    /** Passo 3: Leitura de um registo específico por ID (para ecrã de edição). */
    @Query("SELECT * FROM diario_emocoes WHERE id = :id LIMIT 1")
    LiveData<RegistoEmocao> obterRegistoPorId(long id);

    /** Passo 4: Contagem total — painel de privacidade RGPD Art. 15.º */
    @Query("SELECT COUNT(*) FROM diario_emocoes")
    LiveData<Integer> contarRegistos();

    // -------------------------------------------------------------------------
    // U — UPDATE
    // -------------------------------------------------------------------------
    /**
     * Passo 5: Actualização explícita via @Update.
     * O Room identifica o registo pelo @PrimaryKey (id) do objecto passado
     * e actualiza todas as colunas restantes — SQL gerado automaticamente.
     */
    @Update
    void actualizarRegisto(RegistoEmocao registo);

    // -------------------------------------------------------------------------
    // D — DELETE
    // -------------------------------------------------------------------------
    /** Passo 6: Eliminação por ID — chamada pela lista ao apagar um item. */
    @Query("DELETE FROM diario_emocoes WHERE id = :id")
    void apagarRegistoPorId(long id);

    /**
     * Passo 7: Eliminação total — RGPD Art. 17.º ("Direito ao Esquecimento").
     * Chamada pela PrivacidadeActivity quando o utilizador exerce este direito.
     */
    @Query("DELETE FROM diario_emocoes")
    void apagarTodosOsRegistos();
}
