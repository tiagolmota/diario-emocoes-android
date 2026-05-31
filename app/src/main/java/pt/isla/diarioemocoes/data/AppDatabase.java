package pt.isla.diarioemocoes.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * BASE DE DADOS: AppDatabase
 *
 * Passo 1: @Database declara ao Room quais entidades compõem o esquema.
 * 'version = 1' é o número de versão do esquema. Se no futuro adicionarmos
 * uma coluna ou tabela nova, este número deve ser incrementado e uma
 * Migration implementada — caso contrário a app lança IllegalStateException.
 * 'exportSchema = false' desativa a exportação do histórico de esquema para
 * ficheiro JSON (aceitável em protótipo; num projeto de produção seria 'true').
 */
@Database(entities = {RegistoEmocao.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Passo 2: Método abstrato que expõe o contrato DAO.
     * O Room gera a implementação concreta; a camada de cima (ViewModel)
     * nunca sabe que por baixo existe SQLite — princípio de abstração.
     */
    public abstract RegistoEmocaoDao registoDao();

    // =========================================================================
    // Passo 3: PADRÃO SINGLETON com Double-Checked Locking (Thread-Safe)
    //
    // Fundamento: Criar múltiplas instâncias de RoomDatabase é caro em memória
    // e pode causar corrupção de dados em escritas concorrentes.
    // O 'volatile' garante que qualquer thread vê imediatamente o valor
    // atualizado de INSTANCE — sem cache de CPU a interferir.
    // =========================================================================
    private static volatile AppDatabase INSTANCE;

    /**
     * Passo 4: Método de acesso à instância única.
     * 'synchronized(AppDatabase.class)' previne race conditions:
     * se duas threads chegarem simultaneamente ao bloco null-check,
     * apenas uma cria a instância — a outra encontrará INSTANCE já preenchido.
     *
     * Usamos 'context.getApplicationContext()' e não a Activity diretamente,
     * para garantir que a base de dados não retém uma referência a um ecrã
     * que pode ser destruído e recriado (ex: rotação do dispositivo).
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                // Segunda verificação dentro do bloco sincronizado
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "diario_emocoes_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
