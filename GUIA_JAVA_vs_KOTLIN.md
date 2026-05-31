# GUIA DE REFERÊNCIA: Java vs Kotlin — Diário de Emoções
## Mestrado METSW — DAM | ISLA Santarém | Prof. Marco Tereso

---

## 1. Mapa de Ficheiros do Projeto (Versão Java)

```
DiarioEmocoes_Java/
├── app/
│   ├── build.gradle                          ← Dependências (Room, Lifecycle, RecyclerView)
│   └── src/main/
│       ├── AndroidManifest.xml               ← SplashActivity como LAUNCHER
│       ├── java/pt/isla/diarioemocoes/
│       │   ├── data/
│       │   │   ├── RegistoEmocao.java        ← Entidade (POJO + @Entity)
│       │   │   ├── RegistoEmocaoDao.java     ← Interface DAO (@Dao)
│       │   │   ├── AppDatabase.java          ← Singleton RoomDatabase
│       │   │   └── RegistoEmocaoRepository.java ← Mediador + ExecutorService
│       │   └── ui/
│       │       ├── RegistoEmocaoViewModel.java  ← AndroidViewModel
│       │       ├── RegistoAdapter.java          ← ListAdapter + DiffUtil
│       │       ├── MainActivity.java            ← Activity principal (CRUD + Toast + AlertDialog)
│       │       └── SplashActivity.java          ← Splash Screen (Handler + Intent)
│       └── res/
│           └── layout/
│               ├── activity_splash.xml       ← Ecrã de arranque
│               ├── activity_main.xml         ← Layout principal (ConstraintLayout + Chain)
│               └── item_registo.xml          ← Item da lista (CardView)
```

---

## 2. Tabela Comparativa: Java vs Kotlin (para o teu relatório)

| Conceito                  | Kotlin                              | Java (este projeto)                      |
|---------------------------|-------------------------------------|------------------------------------------|
| **Classe de dados**       | `data class` (automático)           | POJO com Getters/Setters manuais         |
| **Assincronia (escrita)** | `suspend fun` + Coroutines          | `ExecutorService.execute()`              |
| **Assincronia (leitura)** | `Flow<List<T>>`                     | `LiveData<List<T>>`                      |
| **Processador Room**      | `kapt` (Kotlin Annotation Processing)| `annotationProcessor` (Java nativo)     |
| **Null Safety**           | Nativa no compilador (`?`, `!!`)    | `@NonNull` / `@Nullable` (anotações)    |
| **Lambdas**               | Nativas (Java 8+ compatível)        | Requer `compileOptions VERSION_1_8`      |
| **ViewModel**             | `AndroidViewModel(application)`     | `AndroidViewModel(application)` — igual |

---

## 3. Checklist de Requisitos do Enunciado (Prof. Marco Tereso)

| Requisito           | Ficheiro Responsável              | Estado     |
|---------------------|-----------------------------------|------------|
| ✅ Splash Screen    | `SplashActivity.java`             | Implementado |
| ✅ Action Bar       | `AppCompatActivity` (herança)     | Automático via tema |
| ✅ Alert Dialog     | `MainActivity.java` (2 diálogos)  | Guardar + Apagar |
| ✅ CRUD SQL         | `RegistoEmocaoDao.java` + Room    | INSERT + SELECT + DELETE |
| ✅ Toast            | `MainActivity.java`               | Confirmação + Erro + Limpar |
| ⏳ Base Dados Externa | A implementar — Firebase/PHP     | Fase 4 do roadmap |

---

## 4. Justificação Técnica para o Relatório

### Por que LiveData em vez de Flow em Java?

`Flow` é uma API de programação reativa do ecossistema Kotlin/Coroutines.
A sua utilização em Java requer wrappers adicionais (`FlowKt.asLiveData()`)
que introduzem dependências Kotlin numa base de código Java — incoerência arquitetural.

`LiveData` é nativo do Jetpack, funciona identicamente em Java e Kotlin,
e está diretamente integrado no `LifecycleOwner` da Activity.
Para o âmbito deste projeto académico, **LiveData é a escolha correta em Java**.

### Por que ExecutorService em vez de AsyncTask?

`AsyncTask` foi **depreciado no Android API 30 (Android 11)** e removido nas versões
mais recentes. A sua utilização num projeto novo em 2025/2026 seria uma má prática
tecnicamente injustificável. `ExecutorService` com `newSingleThreadExecutor()` é
a substituição recomendada pela documentação oficial da Android Developers
para projetos Java sem Coroutines.

---

## 5. Próxima Fase: Integração com Base de Dados Externa

Para cumprir o requisito de ligação a BD externa, as opções são:

**Opção A — Firebase Realtime Database:**
- Adicionar `google-services.json` ao projeto
- Dependência: `com.google.firebase:firebase-database:20.3.0`
- Sincronização bidirecional Room ↔ Firebase via `ValueEventListener`

**Opção B — PHP + MySQL + JSON:**
- Criar endpoint PHP em servidor (ex: `api/registos.php`)
- Usar `HttpURLConnection` ou a biblioteca `Volley` para HTTP Requests
- Parsear JSON com `org.json` (incluído no Android SDK — sem dependência extra)

A estratégia recomendada: **Room como fonte de verdade local**,
Firebase/PHP como camada de sincronização remota.
Offline-first: a app funciona sem internet; sincroniza quando há conectividade.
