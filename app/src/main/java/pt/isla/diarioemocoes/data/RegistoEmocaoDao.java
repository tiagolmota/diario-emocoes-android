package pt.isla.diarioemocoes.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * DAO: RegistoEmocaoDao — actualizado com operação DELETE ALL para RGPD Art. 17.º
 *
 * Passo 1: @Dao marca esta interface para geração automática de SQL pelo Room.
 * Passo 2: Adicionado apagarTodosOsRegistos() — direito de eliminação RGPD.
 */
@Dao
public interface RegistoEmocaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirRegisto(RegistoEmocao registo);

    @Query("SELECT * FROM diario_emocoes ORDER BY id DESC")
    LiveData<List<RegistoEmocao>> obterTodosOsRegistos();

    @Query("DELETE FROM diario_emocoes WHERE id = :id")
    void apagarRegistoPorId(long id);

    /**
     * RGPD Art. 17.º — Direito de Eliminação ("Direito ao Esquecimento").
     * Apaga todos os registos pessoais do utilizador de forma irreversível.
     * Chamado pela PrivacidadeActivity quando o utilizador exerce este direito.
     */
    @Query("DELETE FROM diario_emocoes")
    void apagarTodosOsRegistos();

    /**
     * Contagem de registos — útil para o painel de privacidade (Art. 15.º).
     * Permite ao utilizador saber quantos registos estão armazenados.
     */
    @Query("SELECT COUNT(*) FROM diario_emocoes")
    LiveData<Integer> contarRegistos();
}
