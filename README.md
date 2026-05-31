# 📱 Diário de Emoções com Contexto Ambiental

> Aplicação Android nativa desenvolvida em **Java** no âmbito do trabalho prático individual da Unidade Curricular de **Desenvolvimento de Aplicações Móveis** — Mestrado em Engenharia de Tecnologias e Sistemas Web (METSW), ISLA Santarém, 2025/2026.

---

## 🎯 Descrição do Projeto

O **Diário de Emoções** é uma aplicação móvel nativa para Android que permite ao utilizador registar, consultar e eliminar os seus estados emocionais ao longo do tempo, enriquecidos com dados de contexto ambiental (temperatura). O foco de investigação centra-se na **estratégia de persistência híbrida de dados**: persistência local via Room/SQLite e sincronização com base de dados externa (Firebase — Fase 2).

---

## 🏗️ Arquitetura

O projeto segue o padrão **MVVM (Model-View-ViewModel)**, recomendado pela Google para aplicações Android modernas:

```
┌─────────────────────────────────────────────┐
│                    VIEW                      │
│  SplashActivity → MainActivity              │
│  (XML Layouts + RecyclerView + AlertDialog)  │
└───────────────────┬─────────────────────────┘
                    │ observa LiveData
┌───────────────────▼─────────────────────────┐
│                 VIEWMODEL                    │
│         RegistoEmocaoViewModel               │
│       (AndroidViewModel — sem memory leak)   │
└───────────────────┬─────────────────────────┘
                    │ delega
┌───────────────────▼─────────────────────────┐
│               REPOSITORY                    │
│        RegistoEmocaoRepository               │
│     (ExecutorService para I/O assíncrono)    │
└───────────────────┬─────────────────────────┘
                    │ acessa
┌───────────────────▼─────────────────────────┐
│              DATA LAYER                     │
│  AppDatabase (Singleton) + RegistoEmocaoDao  │
│         Room / SQLite local                  │
└─────────────────────────────────────────────┘
```

---

## ✅ Requisitos do Enunciado

| Componente            | Implementação                              | Estado |
|-----------------------|--------------------------------------------|--------|
| **Splash Screen**     | `SplashActivity.java` + Handler (2s)       | ✅ |
| **Action Bar**        | `AppCompatActivity` + tema Material        | ✅ |
| **Alert Dialog**      | Confirmação de limpar + confirmação apagar | ✅ |
| **CRUD SQL**          | Room/SQLite via `RegistoEmocaoDao`         | ✅ (C, R, D) |
| **Toast**             | Feedback em guardar, limpar e apagar       | ✅ |
| **Base Dados Externa**| Firebase Realtime Database                 | ⏳ Fase 2 |

---

## 🛠️ Stack Tecnológico

| Tecnologia            | Versão    | Função                                      |
|-----------------------|-----------|---------------------------------------------|
| Java                  | 8+        | Linguagem de desenvolvimento                |
| Android SDK           | API 24–34 | Plataforma alvo (Android 7.0 → 14)         |
| Room                  | 2.6.1     | Abstração ORM sobre SQLite                  |
| LiveData              | 2.7.0     | Observabilidade reativa (substitui Flow)    |
| AndroidViewModel      | 2.7.0     | Gestão de ciclo de vida sem memory leaks    |
| ConstraintLayout      | 2.1.4     | Layouts responsivos e planos                |
| RecyclerView          | 1.3.2     | Lista eficiente com reciclagem de vistas    |
| CardView              | 1.0.0     | Contentor Material para itens de lista      |
| ExecutorService       | JDK nativo | Operações de I/O em background thread      |

---

## 📁 Estrutura do Projeto

```
app/src/main/
├── java/pt/isla/diarioemocoes/
│   ├── data/
│   │   ├── RegistoEmocao.java          # Entidade Room (POJO + @Entity)
│   │   ├── RegistoEmocaoDao.java       # Interface DAO (@Insert, @Query, @Delete)
│   │   ├── AppDatabase.java            # RoomDatabase Singleton (Double-Checked Locking)
│   │   └── RegistoEmocaoRepository.java # Mediador + ExecutorService
│   └── ui/
│       ├── SplashActivity.java         # Ecrã de arranque (Handler + Intent)
│       ├── MainActivity.java           # Activity principal (CRUD + Toast + AlertDialog)
│       ├── RegistoEmocaoViewModel.java # AndroidViewModel (ponte UI ↔ Repository)
│       └── RegistoAdapter.java         # ListAdapter + DiffUtil (RecyclerView)
└── res/
    └── layout/
        ├── activity_splash.xml         # Layout do Splash Screen
        ├── activity_main.xml           # Layout principal (ConstraintLayout + Chain)
        └── item_registo.xml            # Item da lista (CardView)
```

---

## 🚀 Como Executar

### Pré-requisitos

- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 11 ou superior
- Android SDK API 24 mínimo instalado
- Dispositivo físico ou emulador Android 7.0+

### Passos

```bash
# 1. Clonar o repositório
git clone https://github.com/[SEU_USERNAME]/diario-emocoes-android.git

# 2. Abrir no Android Studio
# File → Open → selecionar a pasta clonada

# 3. Sincronizar dependências Gradle
# Android Studio executa automaticamente ao abrir
# Ou manualmente: Build → Sync Project with Gradle Files

# 4. Executar
# Run → Run 'app' (Shift+F10)
# Selecionar dispositivo/emulador
```

---

## 🔑 Decisões Arquiteturais Relevantes

### Chave Primária: `long` (timestamp) vs `String` (data formatada)

Optou-se pelo timestamp em milissegundos (`System.currentTimeMillis()`) como chave primária por três razões objetivas:

1. **Unicidade absoluta** — suporta múltiplos registos no mesmo dia, impossível com uma `String` de data.
2. **Performance de indexação** — o SQLite indexa inteiros via B-Tree com custo O(log n); ordenação por `long` é nativa sem conversão.
3. **Eliminação de parsing** — a conversão para formato legível é feita uma única vez na inserção (`SimpleDateFormat`), não em cada leitura.

### LiveData vs Flow

Em Java puro, `Flow` requer wrappers do ecossistema Kotlin (`FlowKt`) que introduzem dependências desnecessárias. `LiveData` é nativo do Jetpack, integrado com `LifecycleOwner`, e elimina o risco de observar dados após a destruição da Activity — comportamento idêntico a `Flow`, sem overhead.

### ExecutorService vs AsyncTask

`AsyncTask` foi **depreciado na API 30** e removido nas versões recentes do Android. A substituição recomendada pela documentação oficial é `ExecutorService`, que oferece controlo explícito sobre threads e evita os problemas de lifecycle que tornaram `AsyncTask` inseguro.

---

## 📋 Roteiro de Desenvolvimento

- [x] **Fase 1** — Persistência local (Room + MVVM + UI base)
- [ ] **Fase 2** — Integração Firebase Realtime Database
- [ ] **Fase 3** — Leitura de sensor de temperatura (SensorManager)
- [ ] **Fase 4** — Exportação de dados + partilha

---

## 📚 Referências Bibliográficas

- Google LLC. (2024). *Android Developers: Official Documentation and SDK*. https://developer.android.com
- Griffiths, D., & Griffiths, D. (2024). *Head First Android Development* (3rd ed.). O'Reilly Media.
- Sommerhoff, P. (2024). *Kotlin for Android App Development*. Pearson Education (US).
- Payload Media, Inc. (2024). *Android Studio Koala Essentials — Java Edition*. Payload Media.

---

## 📄 Declaração de Uso de IAGen

Em conformidade com o artigo 3.º do Guião de Procedimentos de IA Generativa adotado pelas IES da Lusófona, declara-se que ferramentas de IA Generativa foram utilizadas como **tutor inteligente transacional** no auxílio à compreensão de sintaxe Java/Android e validação concetual do fluxo assíncrono. Toda a arquitetura, lógica de dados e decisões técnicas foram verificadas face à documentação oficial da Android Developers.

---

## 👨‍💻 Autor

| Nome | Número | Curso |
|------|--------|-------|
| Tiago Santos Mota | [Nº Aluno] | METSW — Engenharia de Tecnologias e Sistemas Web |

---

*ISLA Santarém — Escola Superior de Engenharia e Tecnologia | METSW 2025/2026*
