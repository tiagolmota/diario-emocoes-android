package pt.isla.diarioemocoes.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * ENTIDADE: RegistoEmocao
 *
 * Passo 1: A anotação @Entity instrui o compilador do Room a criar
 * uma tabela SQLite com o nome definido em 'tableName'.
 * Em Java, utilizamos classes POJO (Plain Old Java Object) em vez de
 * data classes Kotlin — a lógica é idêntica, a sintaxe difere.
 */
@Entity(tableName = "diario_emocoes")
public class RegistoEmocao {

    /**
     * Passo 2: Chave primária definida como timestamp em milissegundos (long).
     * Justificação arquitetural: um long ocupa 8 bytes, permite indexação
     * binária eficiente pelo SQLite e garante unicidade absoluta mesmo com
     * múltiplos registos no mesmo dia — algo impossível com uma String de data.
     */
    @PrimaryKey
    private long id;

    // Passo 3: Campos da entidade — cada variável private é uma coluna na tabela.
    private String dataHoraLegivel;      // String formatada para exibição na UI
    private String estadoEmocional;      // Ex: "Ansioso", "Motivado", "Calmo"
    private double temperaturaAmbiente;  // Dados contextuais do sensor
    private String notasTexto;           // Texto livre do diário

    /**
     * Passo 4: Construtor completo obrigatório para o Room.
     * O Room instancia os objetos ao ler da base de dados através deste construtor.
     * Em Java, ao contrário do Kotlin, temos de o declarar explicitamente.
     */
    public RegistoEmocao(long id, String dataHoraLegivel, String estadoEmocional,
                         double temperaturaAmbiente, String notasTexto) {
        this.id = id;
        this.dataHoraLegivel = dataHoraLegivel;
        this.estadoEmocional = estadoEmocional;
        this.temperaturaAmbiente = temperaturaAmbiente;
        this.notasTexto = notasTexto;
    }

    // =========================================================================
    // Passo 5: Getters e Setters — obrigatórios em Java para encapsulamento.
    // O Room utiliza estes métodos para ler e escrever os valores dos campos.
    // =========================================================================

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDataHoraLegivel() { return dataHoraLegivel; }
    public void setDataHoraLegivel(String dataHoraLegivel) { this.dataHoraLegivel = dataHoraLegivel; }

    public String getEstadoEmocional() { return estadoEmocional; }
    public void setEstadoEmocional(String estadoEmocional) { this.estadoEmocional = estadoEmocional; }

    public double getTemperaturaAmbiente() { return temperaturaAmbiente; }
    public void setTemperaturaAmbiente(double temperaturaAmbiente) { this.temperaturaAmbiente = temperaturaAmbiente; }

    public String getNotasTexto() { return notasTexto; }
    public void setNotasTexto(String notasTexto) { this.notasTexto = notasTexto; }
}
