# Graphing-Calculator als modulare Bibliothek – Architektur- und Implementierungsplan

## 1) Architekturvorschlag

### Zielarchitektur (MVC + Library-First)

Der Graphing-Calculator wird als **eigenständiger Kern** unter `com.mlprograms.justmath.graphing` aufgebaut und in klar getrennte Module aufgeteilt. Die existierende JavaFX-Schicht (`graphfx.planar.view`) bleibt ein **Adapter**, der ausschließlich öffentliche Kern-APIs nutzt.

- **Model (Core):** Parsing, Kompilierung, Evaluation, Sampling, Ergebnisstrukturen.
- **View (GUI):** JavaFX-Renderer/Canvas, Achsen, Interaktion (Zoom/Pan).
- **Controller (Orchestrierung):** Brücke zwischen GUI-Ereignissen und Core-Services.

### Paketstruktur (Vorschlag)

```text
com.mlprograms.justmath.graphing
├─ api
│  ├─ GraphingCalculator
│  ├─ PlotRequest
│  ├─ PlotResponse
│  ├─ PlotSeries
│  ├─ Domain
│  ├─ Resolution
│  └─ GraphingException (+ Spezialisierungen)
├─ engine
│  ├─ ExpressionCompiler
│  ├─ CompiledExpression
│  ├─ EvaluationContext
│  ├─ SamplingEngine
│  ├─ sampling
│  │  ├─ UniformSampler
│  │  ├─ AdaptiveSampler
│  │  └─ SamplingStrategy
│  └─ cache
│     ├─ ExpressionCache
│     └─ ContextPool
├─ model
│  ├─ PointSeries
│  ├─ PointBuffer
│  ├─ PlotMeta
│  └─ DiscontinuityMarker
└─ gui
   ├─ GraphingController
   ├─ GraphingViewAdapter
   └─ JavaFxPlotRenderer
```

### Schnittstellen und Verantwortlichkeiten

- `api`: stabile Einstiegspunkte für Bibliotheksnutzer.
- `engine`: interne Hochleistungslogik, austauschbare Strategien.
- `model`: immutable/near-immutable Datentransferobjekte zwischen Core und Renderer.
- `gui`: optionale Implementierung, die nur `api` verwendet.

Diese Struktur orientiert sich am bestehenden Stil mit klaren Utility/Engine-Klassen (`CalculatorEngine`, `GraphFxCalculatorEngine`) und separaten Model-Objekten (`PlotPoint`, `PlotLine`, `PlotResult`).

---

## 2) Klassendesign (zentrale Klassen + Beispiel-Signaturen)

### Public API

```java
package com.mlprograms.justmath.graphing.api;

public interface GraphingCalculator {

    PlotResponse plot(String expression, Domain domain, Resolution resolution);

    PlotResponse plot(PlotRequest request);

    List<PlotSeries> plotAll(List<PlotRequest> requests);

    CompiledPlotExpression compile(String expression);

    PlotSeries sample(CompiledPlotExpression expression, PlotRequest request);
}
```

```java
public record Domain(double minX, double maxX) {
    public Domain {
        if (Double.isNaN(minX) || Double.isNaN(maxX) || minX >= maxX) {
            throw new IllegalArgumentException("Invalid domain");
        }
    }
}
```

```java
public record Resolution(int targetSamples, double pixelWidthHint) {
    public static Resolution forPixels(int widthPx) { ... }
    public static Resolution fixed(int samples) { ... }
}
```

```java
public final class PlotRequest {
    private final String expression;
    private final Domain domain;
    private final Resolution resolution;
    private final SamplingStrategy samplingStrategy;
    private final Map<String, Double> variables;
    private final AxisSettings axisSettings;
    // Builder analog zu bestehendem Utility-Stil
}
```

### Engine intern

```java
final class DefaultGraphingCalculator implements GraphingCalculator {
    private final ExpressionCompiler compiler;
    private final SamplingEngine samplingEngine;
    private final ExpressionCache cache;
}
```

```java
public interface ExpressionCompiler {
    CompiledPlotExpression compile(String expression);
}

public interface CompiledPlotExpression {
    double evaluate(double x, EvaluationContext context);
    Set<String> requiredVariables();
}
```

```java
public interface SamplingEngine {
    PlotSeries sample(CompiledPlotExpression expression, PlotRequest request);
}
```

```java
public interface SamplingStrategy {
    PointSeries sample(CompiledPlotExpression expression,
                       Domain domain,
                       Resolution resolution,
                       EvaluationContext context);
}
```

### Fehlerbehandlung

- `GraphingParseException`
- `GraphingEvaluationException`
- `MissingVariableException`
- `NonFiniteResultException`

Alle erben von `GraphingException` (unchecked), analog zu bestehender Fehlerhierarchie in `calculator.exceptions`.

---

## 3) Datenfluss (vom Ausdruck bis Darstellung)

1. **API-Aufruf**: `GraphingCalculator.plot("x^2", domain, resolution)`.
2. **Kompilierung/Cache-Lookup**:
   - Hash des Expressions-Strings + Modus (Grad/Rad, Funktionen).
   - Bei Cache-Hit: direkt `CompiledPlotExpression` nutzen.
3. **Kontextbereitstellung**:
   - `EvaluationContext` aus Pool (ThreadLocal/Object-Pool).
   - Variablen setzen (`x`, optionale Parameter).
4. **Sampling**:
   - Uniform oder Adaptive Strategy.
   - Ergebnis in `PointBuffer` (primitive Arrays) schreiben.
5. **Post-Processing**:
   - Nicht-finite Werte (`NaN`, `Infinity`) als Segmenttrennung markieren.
   - Optional Glättung/Clipping an sichtbaren Bereich.
6. **Response**:
   - `PlotSeries` + `PlotMeta` (Samples, Dauer, Strategie, Warnings).
7. **GUI-Mode**:
   - `GraphingController` nimmt `PlotResponse` und mapped auf JavaFX-Pfade.

---

## 4) Performance-Konzept (Optimierungen + Trade-offs)

### 4.1 Token-/AST-/Postfix-Caching

- Einmaliges Parsen pro Ausdruck, danach Wiederverwendung.
- LRU-Cache (z. B. 256–2048 Expressions, konfigurierbar).
- Trade-off: höherer Speicherbedarf vs. deutlich geringere Latenz bei wiederholten Plots.

### 4.2 Wiederverwendbare EvaluationContext-Objekte

- Vermeidet Allokationen pro Punkt.
- `ThreadLocal<EvaluationContext>` für Single-Thread-Renderloops.
- Pool für Parallelpfade.
- Trade-off: komplexeres Lifecycle-Management.

### 4.3 Datenstrukturen für Punktmassen

- `PointBuffer` mit `double[] xs`, `double[] ys`, `int size`.
- Optional segmentierte Indizes (`int[] breaks`) statt viele kleine Listen.
- Trade-off: weniger API-Bequemlichkeit intern, aber deutlich GC-freundlicher.

### 4.4 Parallelisierung

- Domain in Buckets aufteilen (`ForkJoinPool` / `parallelStream` vermeiden bei enger Kontrolle).
- Nur bei großen Sample-Mengen aktivieren (Schwellwert z. B. > 100k Punkte).
- deterministische Zusammenführung der Buckets.
- Trade-off: Overhead bei kleinen Datensätzen, daher heuristische Aktivierung.

### 4.5 Adaptive Sampling

- Start mit grober Unterteilung, rekursiv verfeinern bei hoher lokaler Krümmung oder großem Fehler gegen lineare Interpolation.
- Stopkriterien: `maxDepth`, `maxSamples`, `epsilon`.
- Vorteil: weniger Punkte bei gleicher visueller Qualität.
- Trade-off: ungleichmäßige Punktverteilung (manche Renderer erwarten gleichmäßige Schritte).

### 4.6 Speicher-/Render-Effizienz

- Headless: `PlotSeries` als `DoubleBuffer`/primitive Arrays exponieren.
- GUI: direkte Übergabe in `PathElement`-Generator ohne Zwischenlisten.
- Optional Streaming-API (`PlotChunkConsumer`) für sehr große Domains.

---

## 5) Integrationskonzept mit bestehender GUI

### Aktueller Bestand

`graphfx.planar.view.GraphFxViewer` und `graphfx.planar.calculator.GraphFxCalculatorEngine` besitzen bereits Trennung zwischen Darstellung und Berechnung. Diese wird zum neuen `graphing.api.GraphingCalculator` migriert.

### Zielintegration

- `GraphFxCalculatorEngine` wird Adapter/Fassade, intern delegierend an `GraphingCalculator`.
- `GraphFxViewer` bleibt unverändert in UX-Logik (Pan/Zoom), nutzt aber neue DTOs (`PlotSeries`).
- Schrittweise Kompatibilitätslayer:
  - Mapping `PlotSeries` -> existierende `PlotLine/PlotPoint` für Übergangszeit.
  - Danach direkte Renderer-Anbindung auf primitive Buffers.

### Rückwärtskompatibilität

- Bestehende öffentliche `graphfx`-Methoden als `@Deprecated` Wrapper beibehalten (mind. 1 Minor-Release).
- Gleiches Verhalten für Fehlermeldungen soweit möglich.

---

## 6) Migrations-/Implementierungsplan in Phasen

### Phase 0 – Analyse/Vertrag
- Öffentliche Ziel-API festschreiben (JavaDoc + Kompatibilitätsregeln).
- Performance-Baseline (Zeit/Speicher) mit bestehender GraphFx-Engine messen.

### Phase 1 – Core API + Compiler
- `graphing.api` + `engine.ExpressionCompiler` implementieren.
- Reuse der vorhandenen `calculator`-Bausteine (Tokenizer/Postfix/Evaluator) statt Doppelimplementierung.
- Unit-Tests für Parsing/Evaluation mit `x`-Variablen.

### Phase 2 – Sampling Engine
- UniformSampler + AdaptiveSampler implementieren.
- `PointBuffer` + Segmentmarker einführen.
- Benchmarks für 1k/10k/100k/1M Samples.

### Phase 3 – GUI Adapter
- `GraphFxCalculatorEngine` auf neue API umstellen.
- Mapping-Layer + visuelle Regressionstests (Screenshots) hinzufügen.

### Phase 4 – Parallelisierung + Feintuning
- Optionales Multi-Thread Sampling (Feature-Flag).
- Cache-/Pool-Größen konfigurierbar machen.

### Phase 5 – Stabilisierung/Release
- Deprecation-Warnungen, Migration Guide, Changelog.
- Lasttests + Profiling (CPU/GC).

---

## 7) Beispiel-API-Nutzung

### Headless-Use-Case

```java
GraphingCalculator calculator = GraphingCalculators.createDefault();

PlotResponse response = calculator.plot(
    "sin(x) + x^2/10",
    new Domain(-10.0, 10.0),
    Resolution.forPixels(1920)
);

double[] xs = response.series().getFirst().xValues();
double[] ys = response.series().getFirst().yValues();
// Weitergabe an externes Diagrammtool (JFreeChart, XChart, etc.)
```

### GUI-Use-Case

```java
GraphingCalculator calculator = GraphingCalculators.createDefault();
GraphingController controller = new GraphingController(calculator, javaFxRenderer);

controller.plot(new PlotRequest.Builder("x^3 - 2*x")
    .domain(new Domain(-5, 5))
    .resolution(Resolution.forPixels(1200))
    .samplingStrategy(SamplingStrategies.adaptive())
    .build());
```

---

## 8) Teststrategie

### 8.1 Korrektheit

- Parser-/Evaluator-Tests für Standardfunktionen, Operatorpriorität, Klammern.
- Referenztests gegen bekannte Werte (z. B. `x^2`, `sin(x)`, Polynomfälle).
- Vergleich Adaptive vs. Uniform in Fehlertoleranzen.

### 8.2 Grenzfälle

- Diskontinuitäten: `1/x`, `tan(x)` in kritischen Bereichen.
- Nicht-definierte Bereiche: `sqrt(x)` bei `x < 0`, `log(x)` bei `x <= 0`.
- Große und kleine Domains (z. B. `1e-9 .. 1e9`).
- Fehlende Variablen und zyklische Definitionen.

### 8.3 Performance

- Mikrobenchmarks für Compile vs. Evaluate vs. Sample.
- Lasttests mit Multi-Function-Plots und 1M+ Samples.
- GC-Pressure-Metriken (Allokationen pro 100k Punkte).

### 8.4 Integrations-/GUI-Tests

- Snapshot-/Screenshot-Regressionen für repräsentative Funktionen.
- Interaktionstests für Zoom/Pan + Replot.
- Sicherstellen, dass GUI denselben Core nutzt wie Headless.

---

## JavaDoc- und API-Qualitätsregeln

- Jede Public-Klasse erhält JavaDoc mit:
  - Thread-Safety-Hinweis,
  - Performance-Charakteristik,
  - Fehlerverhalten (geworfene Exceptions),
  - Beispielnutzung.
- API-Methoden stabil halten; Erweiterungen bevorzugt über Builder/Optionen statt Breaking Changes.

## Empfohlene Sofortmaßnahmen (nächster Sprint)

1. `graphing.api` Minimal-API und `DefaultGraphingCalculator` anlegen.
2. `ExpressionCompiler` auf bestehende `calculator`-Pipeline aufsetzen.
3. `UniformSampler` + `PointBuffer` inkl. Tests implementieren.
4. Adapter in `GraphFxCalculatorEngine` als internen Feature-Flag integrieren.
5. Performance-Baseline dokumentieren und gegen neue Engine vergleichen.
