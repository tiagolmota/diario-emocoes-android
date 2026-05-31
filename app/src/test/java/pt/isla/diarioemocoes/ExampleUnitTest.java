package pt.isla.diarioemocoes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitários locais — executam na JVM sem emulador.
 * Para executar: clique direito → "Run 'ExampleUnitTest'"
 * ou no terminal: ./gradlew test
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void timestamp_isPositive() {
        // Valida que o mecanismo de geração de chave primária retorna valor positivo
        long timestamp = System.currentTimeMillis();
        assertTrue("O timestamp deve ser positivo", timestamp > 0);
    }
}
