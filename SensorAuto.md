# Roadify Logger — Arquitetura, Estrutura e Estratégia de Desenvolvimento

## Visão Geral

Aplicativo Android nativo de **inspeção viária por smartphone**, que usa acelerômetro, giroscópio, GPS e câmera para capturar o comportamento do veículo sobre o pavimento e aproximar indicadores como o IRI (International Roughness Index). Os dados são salvos localmente em **SQLite (Room)** para consulta e em **CSV** para exportação e análise externa.

**Pacote:** `br.com.roadifylogger`
**SDK Mínimo:** 26 | **Target:** 36 | **Java:** 11

---

## Resumo do App

O Roadify Logger transforma o smartphone em um perfilômetro de baixo custo. Fixado no painel do veículo, o aparelho coleta dados brutos dos sensores durante um trajeto comum. Cada sessão de gravação gera arquivos CSV por sensor (acelerômetro, giroscópio, GPS) e opcionalmente imagens da câmera, todos agrupados em uma pasta nomeada com o trecho e a data. O conjunto de dados é a matéria-prima para calcular índices de irregularidade do pavimento em pós-processamento.

---

## Stack Tecnológica

| Categoria           | Tecnologia                                   | Versão / Nota                  |
|---------------------|----------------------------------------------|-------------------------------|
| Linguagem           | Kotlin                                       | 2.2.10                        |
| UI                  | Jetpack Compose + Material3                  | BOM 2024.09.00                |
| Navegação           | Compose Navigation (TypeSafe)                | 2.9.7                         |
| DI                  | Koin                                         | 4.1.1                         |
| Banco local         | Room (SQLite)                                | 2.7.x                         |
| Sensores físicos    | Android SensorManager                        | nativo                        |
| GPS                 | FusedLocationProviderClient (Play Services)  | nativo                        |
| Câmera              | CameraX                                      | 1.4.x                         |
| CSV                 | OpenCSV ou geração manual com BufferedWriter | —                             |
| Coroutines / Flow   | Kotlinx Coroutines                           | 1.10.x                        |
| Background          | WorkManager                                  | 2.10.x (export em background) |
| Serialização        | Kotlinx Serialization JSON                   | 1.10.0 (metadata.json)        |
| Versioning          | Gradle Version Catalog (libs.versions.toml)  | —                             |
| Testes              | JUnit 4 + Espresso + Turbine (Flow tests)    | —                             |

> **Firebase removido.** O app é 100% offline-first. Não há auth, cloud sync nem Firestore.

---

## Design System — Tokens Globais

Baseado nos padrões visuais observados nos mockups:

```kotlin
// ui/theme/Color.kt
val OrangeAmber    = Color(0xFFFF8C00)   // ação, timer ativo, seleção
val GreenSuccess   = Color(0xFF4CAF50)   // status OK, nivelado, ativo
val RedAlert       = Color(0xFFE53935)   // GPS LOST, erro, calib req
val YellowWaiting  = Color(0xFFFFC107)   // buscando, atenção neutra
val CardBackground = Color(0xFFFFFFFF)
val SurfaceDark    = Color(0xFF1A1A2E)

// ui/theme/Shape.kt
val CardRadius    = 14.dp
val BadgeRadius   = 20.dp
val ButtonRadius  = 12.dp
```

Componentes que devem ser **tokens compartilhados** entre todas as telas:
- `StatusBadge(text, color)` — pills coloridas de status
- `SectionCard(title, icon, content)` — card branco com título e ícone
- `LabeledValue(label, value)` — rótulo em caixa-alta + valor em bold
- `SyncedSlider(value, range, onValueChange)` — slider laranja + campo numérico bidirecional
- `PrimaryButton` / `OutlineButton` — padrão laranja preenchido / contorno

---

## Estrutura de Pastas

```
app/src/main/java/br/com/roadifylogger/
│
├── data/
│   ├── datasource/
│   │   ├── AccelerometerDataSource    # SensorEventListener → Flow<AccelReading>
│   │   ├── GyroscopeDataSource        # SensorEventListener → Flow<GyroReading>
│   │   ├── GPSDataSource              # FusedLocationProvider → Flow<GpsReading>
│   │   ├── CameraDataSource           # CameraX ImageCapture / Preview
│   │   └── AudioDataSource            # MediaRecorder (futuro)
│   │
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt         # Room Database (SQLite)
│   │   │   ├── dao/
│   │   │   │   ├── SessionDao         # CRUD de sessões
│   │   │   │   ├── VehicleDao         # CRUD de perfis de veículo
│   │   │   │   ├── DeviceDao          # CRUD de perfis de dispositivo
│   │   │   │   └── SensorConfigDao    # Configurações por sessão
│   │   │   └── converter/
│   │   │       └── Converters.kt      # TypeConverters (enums → String)
│   │   └── csv/
│   │       ├── CsvWriter              # Grava linha a linha (flush incremental)
│   │       └── CsvExporter            # Agrupa e empacota pasta da sessão
│   │
│   ├── model/                         # Entidades Room (anotações @Entity)
│   │   ├── SessionEntity.kt
│   │   ├── VehicleEntity.kt
│   │   ├── DeviceEntity.kt
│   │   └── SensorConfigEntity.kt
│   │
│   └── repository/
│       ├── SessionRepositoryImpl
│       ├── VehicleRepositoryImpl
│       ├── DeviceRepositoryImpl
│       └── SensorConfigRepositoryImpl
│
├── domain/
│   ├── datasource/                    # Interfaces de abstração dos sensores
│   │   ├── IAccelerometerDataSource
│   │   ├── IGyroscopeDataSource
│   │   ├── IGPSDataSource
│   │   └── ICameraDataSource
│   │
│   ├── model/                         # Modelos puros de domínio (sem Room)
│   │   ├── Session.kt                 # id, name, startAt, endAt, distance, status
│   │   ├── Vehicle.kt                 # brand, model, year, engine, gear, motorType
│   │   ├── DeviceProfile.kt           # brand, model, year, mountOrientation
│   │   ├── SensorConfig.kt            # sensorType, enabled, frequencyHz
│   │   ├── AccelReading.kt            # timestamp, x, y, z
│   │   ├── GyroReading.kt             # timestamp, roll, pitch, yaw
│   │   ├── GpsReading.kt              # timestamp, lat, lng, speed, accuracy, satellites
│   │   └── RecordingFile.kt           # sessionId, folderPath, sizeBytes, syncStatus, calibStatus
│   │
│   ├── repository/
│   │   ├── SessionRepository          # Interface: CRUD + stream de sessão ativa
│   │   ├── VehicleRepository
│   │   ├── DeviceRepository
│   │   └── SensorConfigRepository
│   │
│   └── usecase/
│       ├── StartRecordingUseCase      # Inicia sensores + cria sessão no DB
│       ├── PauseRecordingUseCase
│       ├── StopRecordingUseCase       # Para sensores + flush CSV + salva metadados
│       ├── ExportSessionUseCase       # Empacota pasta para compartilhamento
│       ├── DeleteSessionUseCase       # Soft-delete + remove arquivos físicos
│       ├── ComputeOrientationUseCase  # Pitch/Roll a partir de accel+gyro + orientação do suporte
│       └── EstimateIRIUseCase         # Pós-processamento: cálculo aproximado de IRI por trecho
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   ├── Type.kt
│   │   └── Shape.kt
│   │
│   ├── components/                    # Componentes reutilizáveis (design system)
│   │   ├── common/
│   │   │   ├── StatusBadge.kt
│   │   │   ├── SectionCard.kt
│   │   │   ├── LabeledValue.kt
│   │   │   ├── SyncedSlider.kt
│   │   │   ├── PrimaryButton.kt
│   │   │   └── OutlineButton.kt
│   │   ├── session/
│   │   │   ├── TimerDisplay.kt        # HH:MM:SS.ss em âmbar, tipografia grande
│   │   │   ├── MetricsGrid.kt         # Grid 2x2: distância, velocidade, frames, status
│   │   │   ├── LiveLineChart.kt       # Gráfico de linha com janela deslizante (últimos Ns)
│   │   │   └── SessionControls.kt     # Botões Pausar / Finalizar
│   │   ├── preview/
│   │   │   ├── SensorStatusCard.kt    # Card com badge + valores + mini-gráfico ao vivo
│   │   │   └── GpsStatusCard.kt       # Variante GPS (satélites, precisão, fix)
│   │   ├── files/
│   │   │   ├── RecordingFileCard.kt   # Card de arquivo: nome, data, tamanho, badges
│   │   │   └── FileActionBar.kt       # Ações: gráfico, engrenagem, mais opções
│   │   ├── bubble/
│   │   │   └── BubbleLevelWidget.kt   # Círculo + esfera animada + eixos numéricos
│   │   └── navigation/
│   │       └── MainBottomBar.kt       # Bottom nav: Sessão | Arquivos | Configurações
│   │
│   ├── screen/
│   │   ├── session/
│   │   │   ├── ActiveSessionScreen.kt
│   │   │   └── ActiveSessionViewModel.kt
│   │   ├── preview/
│   │   │   ├── SensorPreviewScreen.kt
│   │   │   └── SensorPreviewViewModel.kt
│   │   ├── config/
│   │   │   ├── equipment/
│   │   │   │   ├── EquipmentConfigScreen.kt
│   │   │   │   └── EquipmentConfigViewModel.kt
│   │   │   └── sensors/
│   │   │       ├── SensorConfigScreen.kt
│   │   │       └── SensorConfigViewModel.kt
│   │   ├── summary/
│   │   │   ├── RecordingSummaryScreen.kt
│   │   │   └── RecordingSummaryViewModel.kt
│   │   ├── files/
│   │   │   ├── RecordingFilesScreen.kt
│   │   │   └── RecordingFilesViewModel.kt
│   │   └── bubble/
│   │       ├── BubbleLevelScreen.kt
│   │       └── BubbleLevelViewModel.kt
│   │
│   └── state/                         # UiState por tela
│       ├── ActiveSessionUiState.kt
│       ├── SensorPreviewUiState.kt
│       ├── EquipmentConfigUiState.kt
│       ├── SensorConfigUiState.kt
│       ├── RecordingSummaryUiState.kt
│       ├── RecordingFilesUiState.kt
│       └── BubbleLevelUiState.kt
│
├── navigation/
│   ├── Screen.kt                      # Sealed classes TypeSafe para todas as rotas
│   └── RoadifyNavHost.kt              # NavHost principal + bottom nav
│
├── worker/
│   └── ExportWorker.kt                # WorkManager: export CSV em background
│
├── KoinModule.kt
└── MainActivity.kt
```

---

## Estrutura de Arquivos no Dispositivo

Cada sessão gera uma pasta nomeada com código de via + descrição + data:

```
/Android/data/br.com.roadifylogger/files/gravacoes/
  /BR-116_Trecho_Sul_2023-11-15/
    acelerometro.csv      # timestamp_ms, x_g, y_g, z_g
    giroscopio.csv        # timestamp_ms, roll_dps, pitch_dps, yaw_dps
    gps.csv               # timestamp_ms, lat, lng, speed_kmh, accuracy_m, satellites
    metadata.json         # veículo, dispositivo, config de sensores,
                          # posição inicial/final, duração, status de calibração
    /imagens/
      frame_00001.jpg
      frame_00002.jpg
      ...
```

**Cabeçalhos dos CSVs:**

```
acelerometro.csv:  timestamp_ms,x_g,y_g,z_g
giroscopio.csv:    timestamp_ms,roll_dps,pitch_dps,yaw_dps
gps.csv:           timestamp_ms,lat,lng,altitude_m,speed_kmh,accuracy_m,satellites,bearing_deg
```

---

## Arquitetura: Clean Architecture + MVVM

```
┌─────────────────────────────────────────────────────────┐
│                        UI LAYER                         │
│    Screen → ViewModel → UiState → Composables           │
└──────────────────────────┬──────────────────────────────┘
                           │ usa interfaces de
┌──────────────────────────▼──────────────────────────────┐
│                     DOMAIN LAYER                        │
│    Repository Interfaces + UseCases + Domain Models     │
└──────────────────────────┬──────────────────────────────┘
                           │ implementado por
┌──────────────────────────▼──────────────────────────────┐
│                      DATA LAYER                         │
│    RepositoryImpl + Room DAOs + DataSources (Sensores)  │
│    SensorManager │ FusedLocation │ CameraX │ CsvWriter  │
└─────────────────────────────────────────────────────────┘
```

**Regra de dependência:** ViewModels nunca conhecem Room ou SensorManager. Repositórios nunca conhecem Compose. Use Cases são puros e testáveis.

---

## Banco de Dados Room (SQLite)

### Tabelas

| Tabela           | Colunas principais                                                                        |
|------------------|------------------------------------------------------------------------------------------|
| `sessions`       | id, name, startAt, endAt, durationMs, distanceKm, initialLat, initialLng, finalLat, finalLng, folderPath, syncStatus, gpsQuality, calibStatus, vehicleId, deviceId |
| `vehicles`       | id, brand, model, year, engine, gearType (MANUAL/AUTO), motorType (COMBUSTION/ELECTRIC)  |
| `devices`        | id, brand, model, year, mountOrientation (VERTICAL/HORIZONTAL), autoDetected              |
| `sensor_configs` | id, sessionId, sensorType (ACCEL/GYRO/GPS/CAMERA/AUDIO), enabled, frequencyHz            |

> Leituras brutas (acelerômetro, giroscópio, GPS) **não vão para o banco** — são escritas diretamente em CSV em tempo real, linha por linha, via `CsvWriter` com `BufferedWriter` para minimizar overhead de I/O. O Room guarda apenas metadados.

---

## Fluxos de Navegação

### Fluxo de pré-sessão (setup completo)
```
EquipmentConfigScreen → SensorConfigScreen → BubbleLevelScreen → SensorPreviewScreen → ActiveSessionScreen
```

### Fluxo de finalização
```
ActiveSessionScreen ("Finalizar") → StopRecordingUseCase (flush CSV + salva DB) → RecordingSummaryScreen
    → [Iniciar nova sessão] → volta ao início do fluxo de pré-sessão
    → [Arquivos]           → RecordingFilesScreen
    → [Compartilhar]       → ExportSessionUseCase → Intent.ACTION_SEND
    → [Remover]            → DeleteSessionUseCase (soft-delete + confirm dialog)
```

### Navegação principal (Bottom Bar — 3 abas)
```
Tab 1: Sessão    → SensorPreviewScreen (ou ActiveSessionScreen se houver sessão ativa)
Tab 2: Arquivos  → RecordingFilesScreen
Tab 3: Config    → EquipmentConfigScreen / SensorConfigScreen (stack próprio)
```

### Acesso ao Nível de Bolha
```
Via aba Config → botão "Calibrar Nível" → BubbleLevelScreen
OU automaticamente como passo do fluxo de pré-sessão (entre SensorConfig e Preview)
```

---

## Detalhamento das Telas e ViewModels

### Tela 01 — Sessão Ativa (`ActiveSessionScreen`)

**UiState:**
```kotlin
data class ActiveSessionUiState(
    val sessionName: String,
    val elapsedMs: Long,
    val distanceKm: Double,
    val speedKmh: Double,
    val frameCount: Int,
    val signalQuality: Int,          // 0–100%
    val recordingStatus: RecordingStatus, // RUNNING, PAUSED, IDLE
    val accelBuffer: List<AccelReading>, // janela deslizante (últimos N pontos)
    val gyroBuffer: List<GyroReading>
)
```

**ViewModel responsabilidades:**
- Assina os Flows dos DataSources de sensor, mantendo buffers deslizantes em memória.
- Acumula distância via integração de velocidade GPS.
- Dispara `StopRecordingUseCase` ao finalizar → navega para Summary.
- Expõe timer via `ticker` com precisão de centésimos.

---

### Tela 02 — Configuração do Equipamento (`EquipmentConfigScreen`)

**UiState:**
```kotlin
data class EquipmentConfigUiState(
    val vehicle: Vehicle,
    val device: DeviceProfile,
    val isSaved: Boolean
)
```

**ViewModel responsabilidades:**
- Carrega último veículo/dispositivo cadastrado do Room.
- Auto-detecta modelo do dispositivo via `Build.MANUFACTURER` / `Build.MODEL`.
- Persiste no Room ao salvar (um perfil de veículo pode ser reaproveitado em múltiplas sessões).
- Orientação do suporte (vertical/horizontal) é propagada para o `ComputeOrientationUseCase`.

---

### Tela 03 — Configuração dos Sensores (`SensorConfigScreen`)

**UiState:**
```kotlin
data class SensorConfigUiState(
    val sensors: List<SensorConfigItem>
)

data class SensorConfigItem(
    val type: SensorType,             // ACCEL, GYRO, GPS, CAMERA, AUDIO
    val isAvailable: Boolean,         // detectado no hardware
    val isEnabled: Boolean,
    val frequencyHz: Int,
    val freqMin: Int,
    val freqMax: Int
)
```

**ViewModel responsabilidades:**
- Consulta disponibilidade real de cada sensor via `SensorManager.getDefaultSensor()`.
- Persiste configurações no Room como `SensorConfigEntity` vinculada à sessão.
- Slider e campo numérico são bidirecionais (sincronia pelo ViewModel, não pela View).

---

### Tela 04 — Pré-visualização dos Sensores (`SensorPreviewScreen`)

**UiState:**
```kotlin
data class SensorPreviewUiState(
    val accel: SensorLiveData,
    val gyro: SensorLiveData,
    val gps: GpsLiveData,
    val allReady: Boolean             // true quando todos os sensores ativos têm status ACTIVE
)

data class SensorLiveData(
    val status: SensorStatus,         // ACTIVE, SEARCHING, UNAVAILABLE
    val values: List<Float>,          // 3 valores (x/y/z ou roll/pitch/yaw)
    val buffer: List<List<Float>>     // mini-gráfico ao vivo
)

data class GpsLiveData(
    val status: SensorStatus,
    val satellites: Int,
    val accuracyMeters: Float,
    val lat: Double?,
    val lng: Double?
)
```

**ViewModel responsabilidades:**
- Assina os DataSources em modo "preview" (sem gravar CSV).
- Botão "Iniciar Gravação" fica desabilitado enquanto `allReady == false` (ou GPS sem fix).
- Ao confirmar, chama `StartRecordingUseCase` → navega para `ActiveSessionScreen`.

---

### Tela 05 — Resumo da Gravação (`RecordingSummaryScreen`)

**UiState:**
```kotlin
data class RecordingSummaryUiState(
    val session: Session,
    val initialAddress: String?,      // reverse geocoding (opcional, pode ser null offline)
    val finalAddress: String?,
    val folderPath: String,
    val fileSizeMb: Double
)
```

**ViewModel responsabilidades:**
- Carrega sessão finalizada do Room por id.
- Tenta reverse geocoding offline (via Geocoder do Android).
- Ações: `delete()`, `share()` (chama `ExportSessionUseCase`), `startNew()` (navega para Preview).

---

### Tela 06 — Arquivos de Gravação (`RecordingFilesScreen`)

**UiState:**
```kotlin
data class RecordingFilesUiState(
    val files: List<RecordingFile>,
    val searchQuery: String,
    val isLoading: Boolean
)

data class RecordingFile(
    val sessionId: String,
    val name: String,
    val date: LocalDate,
    val sizeBytes: Long,
    val syncStatus: SyncStatus,       // LOCAL_ONLY, SYNCED
    val gpsQuality: GpsQuality,       // HIGH, MEDIUM, LOW, LOST
    val calibStatus: CalibStatus      // OK, CALIB_REQ
)
```

**ViewModel responsabilidades:**
- Carrega lista do Room com filtro reativo por `searchQuery`.
- Ações por item: `openStats()`, `openSettings()`, `delete()`, `rename()`, `share()`.

---

### Tela 07 — Nível de Bolha (`BubbleLevelScreen`)

**UiState:**
```kotlin
data class BubbleLevelUiState(
    val pitchDeg: Float,
    val rollDeg: Float,
    val isLevel: Boolean,             // |pitch| < 2° && |roll| < 2°
    val toleranceDeg: Float = 2f
)
```

**ViewModel responsabilidades:**
- Assina `AccelerometerDataSource` + `GyroscopeDataSource` via `ComputeOrientationUseCase`.
- Aplica filtro complementar (accel + gyro fusion) para reduzir ruído.
- Respeita orientação do suporte (vertical/horizontal) para mapear pitch/roll corretamente.
- Anima posição da "bolha" no `BubbleLevelWidget` com suavização (Animatable).

---

## Use Cases — Regras de Negócio

| Use Case                   | Entrada                          | Saída / Efeito                                          |
|---------------------------|----------------------------------|---------------------------------------------------------|
| `StartRecordingUseCase`   | sessionName, vehicleId, deviceId | Cria `Session` no Room, abre `CsvWriter` para cada sensor ativo |
| `PauseRecordingUseCase`   | sessionId                        | Pausa os DataSources, flush parcial dos CSVs             |
| `StopRecordingUseCase`    | sessionId, finalLocation         | Fecha CSVs, salva posição final + tamanho no Room, atualiza status |
| `ExportSessionUseCase`    | sessionId                        | Comprime pasta ou gera Intent com os arquivos CSV        |
| `DeleteSessionUseCase`    | sessionId                        | Soft-delete no Room + remove pasta física do dispositivo |
| `ComputeOrientationUseCase` | AccelReading, GyroReading, orientation | pitch (Float), roll (Float) via filtro complementar |
| `EstimateIRIUseCase`      | List<AccelReading>, List<GpsReading> | Map<GpsSegment, IriEstimate> (pós-processamento)    |

---

## KoinModule — Grafo de Dependências

```kotlin
val roadifyModule = module {

    // Data Sources (singletons — sensor precisa de ciclo de vida longo)
    single<IAccelerometerDataSource> { AccelerometerDataSource(get()) }
    single<IGyroscopeDataSource>     { GyroscopeDataSource(get()) }
    single<IGPSDataSource>           { GPSDataSource(get()) }
    single<ICameraDataSource>        { CameraDataSource(get()) }

    // Room
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().sessionDao() }
    single { get<AppDatabase>().vehicleDao() }
    single { get<AppDatabase>().deviceDao() }
    single { get<AppDatabase>().sensorConfigDao() }

    // CSV
    single { CsvWriter() }
    single { CsvExporter(get()) }

    // Repositories
    single<SessionRepository>     { SessionRepositoryImpl(get(), get()) }
    single<VehicleRepository>     { VehicleRepositoryImpl(get()) }
    single<DeviceRepository>      { DeviceRepositoryImpl(get()) }
    single<SensorConfigRepository>{ SensorConfigRepositoryImpl(get()) }

    // Use Cases
    factory { StartRecordingUseCase(get(), get(), get()) }
    factory { StopRecordingUseCase(get(), get()) }
    factory { ExportSessionUseCase(get()) }
    factory { DeleteSessionUseCase(get()) }
    factory { ComputeOrientationUseCase() }
    factory { EstimateIRIUseCase() }

    // ViewModels
    viewModel { ActiveSessionViewModel(get(), get(), get(), get()) }
    viewModel { SensorPreviewViewModel(get(), get(), get()) }
    viewModel { EquipmentConfigViewModel(get(), get()) }
    viewModel { SensorConfigViewModel(get()) }
    viewModel { (sessionId: String) -> RecordingSummaryViewModel(sessionId, get(), get()) }
    viewModel { RecordingFilesViewModel(get(), get()) }
    viewModel { BubbleLevelViewModel(get(), get(), get()) }
}
```

---

## Estratégia de Desenvolvimento — 6 Fases

### Fase 1 — Fundação (1–2 semanas)
**Objetivo:** Projeto compilando com skeleton completo.

- [ ] Setup do projeto Android com Kotlin + Compose + BOM
- [ ] Gradle Version Catalog (`libs.versions.toml`) com todas as dependências
- [ ] KoinModule esqueleto (declarar tudo, implementar vazio)
- [ ] Room com todas as entidades e DAOs (migrations vazias)
- [ ] Design System: `Color.kt`, `Theme.kt`, `Type.kt`, `Shape.kt`
- [ ] Componentes base: `StatusBadge`, `SectionCard`, `LabeledValue`, `PrimaryButton`
- [ ] Navegação: `Screen.kt` (sealed classes) + `RoadifyNavHost.kt` com rotas stub
- [ ] `MainBottomBar` (3 abas) funcionando com telas placeholder

**Critério de aceite:** app abre, navega entre as 3 abas, tema aplicado.

---

### Fase 2 — Infraestrutura de Sensores (1–2 semanas)
**Objetivo:** DataSources emitindo Flows reais de dados de sensor.

- [ ] `AccelerometerDataSource` com `SensorEventListener` → `callbackFlow<AccelReading>`
- [ ] `GyroscopeDataSource` com o mesmo padrão
- [ ] `GPSDataSource` com `FusedLocationProviderClient` → `callbackFlow<GpsReading>`
- [ ] `CameraDataSource` com CameraX (preview + captura de frame)
- [ ] `ComputeOrientationUseCase` com filtro complementar (accel + gyro fusion)
- [ ] Testes unitários dos Use Cases com dados simulados

**Critério de aceite:** logs mostram leituras contínuas dos 3 sensores em Hz configurado.

---

### Fase 3 — Telas de Configuração (1 semana)
**Objetivo:** O usuário consegue configurar veículo, sensores e calibrar o nível.

- [ ] `EquipmentConfigScreen` + VM completos (auto-detect + persist Room)
- [ ] `SensorConfigScreen` + VM completos (disponibilidade real + slider bidirecional)
- [ ] `BubbleLevelScreen` + VM + `BubbleLevelWidget` animado
- [ ] Persistência Room para Vehicle, Device, SensorConfig

**Critério de aceite:** dados persistem entre sessões do app; nível de bolha reage ao inclinar o celular.

---

### Fase 4 — Fluxo Central de Gravação (2 semanas)
**Objetivo:** Gravar uma sessão completa do início ao fim.

- [ ] `SensorPreviewScreen` + VM (badges de status dinâmicos, botão bloqueado sem GPS fix)
- [ ] `StartRecordingUseCase`: cria Session no Room + abre CsvWriters
- [ ] `CsvWriter` com `BufferedWriter` (flush incremental a cada N linhas)
- [ ] `ActiveSessionScreen` + VM (timer, métricas, buffers deslizantes, gráficos ao vivo)
- [ ] `LiveLineChart` composable (canvas + janela deslizante configurável)
- [ ] `StopRecordingUseCase`: fecha CSVs + salva metadados finais + calcula tamanho da pasta
- [ ] `RecordingSummaryScreen` + VM (reverse geocoding, ações)

**Critério de aceite:** sessão completa gera pasta com 3 CSVs e metadata.json válidos.

---

### Fase 5 — Gerenciamento de Arquivos e Exportação (1 semana)
**Objetivo:** O usuário pode ver, filtrar, exportar e excluir sessões.

- [ ] `RecordingFilesScreen` + VM (lista, busca, badges de status)
- [ ] `RecordingFileCard` + `FileActionBar`
- [ ] `ExportSessionUseCase` (Intent de compartilhamento com pasta ou ZIP)
- [ ] `DeleteSessionUseCase` (soft-delete + diálogo de confirmação + remoção de pasta)
- [ ] `ExportWorker` (WorkManager) para exportação em background de sessões grandes
- [ ] Permissões: `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` (ou Scoped Storage API 29+)

**Critério de aceite:** compartilhar sessão abre seletor de apps com os arquivos CSV; excluir remove do banco e do disco.

---

### Fase 6 — Pós-processamento e Polish (1–2 semanas)
**Objetivo:** Qualidade, IRI estimado e estabilidade.

- [ ] `EstimateIRIUseCase` (implementação do algoritmo de aproximação de IRI por janelas de GPS)
- [ ] Exibição de estimativa de IRI no `RecordingSummaryScreen` (por trecho)
- [ ] Testes de integração dos DAOs com Room in-memory
- [ ] Testes de fluxo com Turbine (Flow testing)
- [ ] Revisão de permissões (câmera, microfone, localização precisa, atividade física)
- [ ] Edge cases: sensor não disponível, GPS perdido durante gravação, disco cheio
- [ ] Ícone do app, splash screen, strings em PT-BR

**Critério de aceite:** app passa por uma gravação completa de 10 minutos sem crash; CSVs são válidos e importáveis em ferramentas de análise.

---

## Mapa de Telas × Dados

| Tela                  | Lê de             | Escreve em            | Sensores ativos            |
|-----------------------|-------------------|-----------------------|----------------------------|
| Equipment Config      | Room (vehicle, device) | Room              | Nenhum                     |
| Sensor Config         | Room (sensorConfig) + SensorManager (disponibilidade) | Room | Nenhum (só consulta) |
| Bubble Level          | —                 | —                     | Acelerômetro + Giroscópio  |
| Sensor Preview        | —                 | —                     | Accel + Gyro + GPS         |
| Active Session        | Room (session)    | CSV (streaming)       | Accel + Gyro + GPS + Câmera|
| Recording Summary     | Room (session)    | Room (status final)   | Nenhum (pós-gravação)      |
| Recording Files       | Room (sessions)   | Room (delete/rename)  | Nenhum                     |

---

## Pontos de Atenção Técnica

| Tema | Detalhe |
|---|---|
| Orientação do suporte | `ComputeOrientationUseCase` aplica rotação de matriz 90° nos eixos quando `mountOrientation == HORIZONTAL`, invertendo quais eixos do sensor bruto mapeiam para pitch e roll. |
| Frequência de I/O do CSV | Não fazer `flush()` a cada linha. Acumular buffer de 50–100 leituras e dar flush em lote — evita gargalo de I/O a 50 Hz. |
| GPS vs. Acelerômetro | GPS a 1 Hz, acelerômetro a 50 Hz. A integração de distância deve ser feita pelo GPS; o acelerômetro complementa nos trechos sem sinal. |
| Scoped Storage | Android 10+ exige `MediaStore` ou pasta específica do app (`getExternalFilesDir`) para evitar permissões WRITE_EXTERNAL_STORAGE. Preferir `getExternalFilesDir()` — não requer permissão a partir do API 29. |
| Ciclo de vida dos sensores | DataSources devem ser singletons com controle explícito de `start()`/`stop()` — não depender do ciclo de vida da Activity para não perder dados ao rotacionar a tela. |
| Câmera em background | CameraX não funciona em background sem `Foreground Service`. A sessão de gravação deve rodar como `Service` quando a tela for minimizada. |
| IRI — algoritmo | Implementação de referência: método de perfil de deslocamento vertical acumulado por janelas de 250 m. Não é IRI padrão ASTM, mas aproximação suficiente para triagem viária. |

---

## Checklist de Migração desde o Template Anterior

| Item | Ação para o Roadify Logger |
|---|---|
| Pacote | `br.edu.utfpr.pb.segundo_projeto...` → `br.com.roadifylogger` |
| Firebase → Room | Remover todas as dependências Firebase; adicionar Room + KSP |
| Modelos | `User/Group/MovementSession` → `Session/Vehicle/Device/SensorConfig` |
| DataSources | `StepCounterDataSource` → `Accel/Gyro/GPS/CameraDataSource` |
| Use Cases | `CalculateScore/ActivityLevel` → `StartRecording/StopRecording/ExportSession/ComputeOrientation/EstimateIRI` |
| Telas | `Login/Cadastro/Home/Ranking/Grupos` → as 7 telas do Roadify |
| Tema | Paleta verde/preto → laranja/âmbar + verde/vermelho/amarelo do Roadify |
| Auth | Remover completamente (app local, sem login) |
| Navigation | Adaptar `Screen.kt` + `NavHost` para as 7 rotas + bottom bar de 3 abas |
| KoinModule | Reescrever completamente com novos bindings |
