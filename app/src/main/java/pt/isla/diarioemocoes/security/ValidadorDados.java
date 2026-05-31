package pt.isla.diarioemocoes.security;

/**
 * VALIDADOR DE DADOS: ValidadorDados
 *
 * Implementa validação e sanitização de todos os inputs do utilizador.
 *
 * FUNDAMENTOS DE SEGURANÇA (OWASP Mobile Top 10):
 * M1 — Improper Credential Usage: não há credenciais, mas os campos de texto
 *       são validados para evitar injecção de dados maliciosos.
 * M7 — Client Code Quality: validação consistente previne estados inválidos na BD.
 *
 * FUNDAMENTO RGPD — Art. 5.º §1.c (Minimização dos Dados):
 * "Os dados pessoais devem ser adequados, pertinentes e limitados ao que é
 * necessário relativamente às finalidades para as quais são tratados."
 * Os limites de caracteres implementados aqui são a expressão técnica
 * deste princípio — não armazenamos mais do que o necessário.
 *
 * FUNDAMENTO RGPD — Art. 5.º §1.d (Exactidão):
 * A validação de campos obrigatórios garante que os dados armazenados
 * são completos e significativos.
 */
public class ValidadorDados {

    // Limites definidos com base no princípio de minimização (Art. 5.º §1.c RGPD)
    public static final int MAX_ESTADO_CHARS = 100;
    public static final int MAX_NOTAS_CHARS  = 1000;
    public static final int MIN_ESTADO_CHARS = 1;

    /**
     * Resultado de validação — encapsula o estado e a mensagem de erro.
     * Padrão Result Object: evita usar excepções para controlo de fluxo normal.
     */
    public static class ResultadoValidacao {
        public final boolean valido;
        public final String mensagemErro;

        private ResultadoValidacao(boolean valido, String mensagem) {
            this.valido = valido;
            this.mensagemErro = mensagem;
        }

        public static ResultadoValidacao ok() {
            return new ResultadoValidacao(true, null);
        }

        public static ResultadoValidacao erro(String mensagem) {
            return new ResultadoValidacao(false, mensagem);
        }
    }

    /**
     * Passo 1: Validar o campo de estado emocional.
     * Obrigatório, com limites mínimo e máximo definidos.
     */
    public static ResultadoValidacao validarEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return ResultadoValidacao.erro("O estado emocional é obrigatório.");
        }
        if (estado.trim().length() > MAX_ESTADO_CHARS) {
            return ResultadoValidacao.erro(
                    "O estado emocional não pode exceder " + MAX_ESTADO_CHARS + " caracteres."
            );
        }
        return ResultadoValidacao.ok();
    }

    /**
     * Passo 2: Validar o campo de notas.
     * Opcional, mas com limite máximo (minimização de dados).
     */
    public static ResultadoValidacao validarNotas(String notas) {
        if (notas != null && notas.length() > MAX_NOTAS_CHARS) {
            return ResultadoValidacao.erro(
                    "As notas não podem exceder " + MAX_NOTAS_CHARS + " caracteres."
            );
        }
        return ResultadoValidacao.ok();
    }

    /**
     * Passo 3: Sanitizar texto — remover caracteres de controlo e
     * normalizar espaços em branco excessivos.
     * Previne armazenamento de dados corrompidos ou sequências de escape.
     */
    public static String sanitizar(String texto) {
        if (texto == null) return "";
        // Remover caracteres de controlo (excepto newline e tab — válidos em notas)
        return texto.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "")
                    .trim();
    }

    /**
     * Passo 4: Validar temperatura ambiental — intervalo fisicamente plausível.
     * Dados de sensores podem devolver valores erráticos; validação defensiva
     * evita armazenar leituras impossíveis.
     */
    public static ResultadoValidacao validarTemperatura(double temperatura) {
        if (temperatura < -50.0 || temperatura > 60.0) {
            return ResultadoValidacao.erro(
                    "Temperatura fora do intervalo válido (-50°C a 60°C)."
            );
        }
        return ResultadoValidacao.ok();
    }
}
