# Guia de Contribuição — Diário de Emoções Android

## Convenções de Commits

Este projeto segue o padrão **Conventional Commits** para manter o histórico legível:

```
<tipo>(<âmbito>): <descrição curta em português>

Tipos permitidos:
  feat     → nova funcionalidade
  fix      → correção de bug
  refactor → refatoração sem mudança de comportamento
  docs     → alterações em documentação
  test     → adição ou correção de testes
  chore    → alterações de build, dependências, configuração
```

### Exemplos

```bash
feat(data): adicionar entidade RegistoEmocao com timestamp como PK
feat(ui): implementar SplashActivity com transição de 2 segundos
fix(viewmodel): corrigir memory leak ao usar ApplicationContext
docs(readme): atualizar secção de decisões arquiteturais
chore(gradle): atualizar Room para versão 2.6.1
```

## Branches

| Branch     | Função                                        |
|------------|-----------------------------------------------|
| `main`     | Código estável — apenas merges revistos       |
| `develop`  | Integração contínua de funcionalidades        |
| `feat/xxx` | Desenvolvimento de funcionalidade específica  |
| `fix/xxx`  | Correção de bug específico                    |

## Estrutura de Pull Request

1. Branch a partir de `develop`
2. Nomear a branch: `feat/nome-da-funcionalidade` ou `fix/descricao-do-bug`
3. Commits atómicos com mensagens descritivas
4. PR com descrição do que foi alterado e porquê
5. Pelo menos um revisor antes de merge

## Código Java — Regras de Estilo

- Nomes de classes: `PascalCase` (`RegistoEmocao`, `AppDatabase`)
- Nomes de métodos e variáveis: `camelCase` (`inserirRegisto`, `timestampAtual`)
- Constantes: `UPPER_SNAKE_CASE` (`SPLASH_DURATION_MS`)
- Cada ficheiro: uma única classe pública
- Comentários de método: JavaDoc quando a lógica não é óbvia
- Sem código comentado no repositório — usar `git stash` para trabalho em curso
