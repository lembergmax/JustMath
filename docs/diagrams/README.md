# JustMath — Diagram Documentation

Technische Architektur- und Ablaufdiagramme für JustMath, im **draw.io / diagrams.net**
Format (`.drawio`, gültiges `<mxfile>`). Jede Datei lässt sich in
[app.diagrams.net](https://app.diagrams.net) öffnen und bearbeiten.

Alle Diagramminhalte sind direkt aus dem Quellcode abgeleitet (Stand: Branch `developer`).
Es wurden keine Klassen oder Methoden erfunden.

## Inhalt

| # | Datei | Typ | Inhalt |
|---|-------|-----|--------|
| 01 | [architecture/01-overall-architecture.drawio](architecture/01-overall-architecture.drawio) | Komponenten | Gesamtüberblick der Pakete `calculator`, `bignumber`, `converter` + Abhängigkeiten |
| 02 | [architecture/02-calculator-engine-api.drawio](architecture/02-calculator-engine-api.drawio) | Klasse/API | Die vier API-Familien von `CalculatorEngine` + Konfiguration |
| 03 | [architecture/03-bignumber-and-math.drawio](architecture/03-bignumber-and-math.drawio) | Klasse | `BigNumber` und Delegation an `math.*` / `BigDecimalMath` |
| 04 | [architecture/04-bignumbermatrix-and-matrixmath.drawio](architecture/04-bignumbermatrix-and-matrixmath.drawio) | Klasse | `BigNumberMatrix` + `MatrixMath` |
| 05 | [architecture/05-bignumberlist-and-sorting.drawio](architecture/05-bignumberlist-and-sorting.drawio) | Klasse/Entscheidung | `BigNumberList` + größenbasierte Sortierer-Wahl |
| 06 | [architecture/06-unit-converter-architecture.drawio](architecture/06-unit-converter-architecture.drawio) | Komponenten | `UnitConverter`-Subsystem (Unit / Registry / Definition / Formula / Value) |
| 07 | [flow/07-expression-pipeline.drawio](flow/07-expression-pipeline.drawio) | Ablauf | Pipeline: String → Tokenizer → Postfix → Evaluator → `BigNumber` |
| 08 | [flow/08-error-handling-flow.drawio](flow/08-error-handling-flow.drawio) | Ablauf | Fehlerklassifizierung, Exception vs. `CalculatorResult`, RAW/USER_FRIENDLY, i18n |
| 09 | [flow/09-caching-flow.drawio](flow/09-caching-flow.drawio) | Ablauf | LRU-Token-Cache (lookup/store) |
| 10 | [flow/10-mode-state-diagrams.drawio](flow/10-mode-state-diagrams.drawio) | State/Entscheidung | `ErrorMode`, `TrigonometricMode`, `CalculatorResult`, Unit-Kompatibilität |
| 18 | [flow/18-detailed-evaluation-trace.drawio](flow/18-detailed-evaluation-trace.drawio) | Trace (Schritt-für-Schritt) | Vollständiger Berechnungs-Trace von `3 + 4 * 2 - ( 1 - 5 )`: Shunting-Yard und RPN-Auswertung mit Operator-Stack, Output-Queue und Value-Stack pro Schritt |
| 19 | [flow/19-detailed-evaluation-trace-function-unary.drawio](flow/19-detailed-evaluation-trace-function-unary.drawio) | Trace (Schritt-für-Schritt) | Trace von `sqrt(16) + -3^2` → −5: Funktionsaufruf, unäres Minus und rechtsassoziatives `^`; zeigt warum `-3^2` = `-(3^2)` |
| 11 | [sequence/11-seq-evaluate-simple.drawio](sequence/11-seq-evaluate-simple.drawio) | Sequenz | `evaluate("2+3*4")` |
| 12 | [sequence/12-seq-evaluate-variables.drawio](sequence/12-seq-evaluate-variables.drawio) | Sequenz | `evaluate("2*x+y", {x=3, y=4})` |
| 13 | [sequence/13-seq-evaluate-error.drawio](sequence/13-seq-evaluate-error.drawio) | Sequenz | `evaluateSafe("1/0")` → Fehlerfall |
| 14 | [sequence/14-seq-matrix-operation.drawio](sequence/14-seq-matrix-operation.drawio) | Sequenz | `BigNumberMatrix.determinant()` (Laplace) |
| 15 | [sequence/15-seq-unit-conversion.drawio](sequence/15-seq-unit-conversion.drawio) | Sequenz | `convert(5, KILOMETER, METER)` |
| 16 | [extension/16-extension-add-function.drawio](extension/16-extension-add-function.drawio) | Guide | Neue Mathe-Funktion über die `ExpressionElements`-Registry hinzufügen |
| 17 | [extension/17-extension-add-unit.drawio](extension/17-extension-add-unit.drawio) | Guide | Neue Einheit über `UnitRegistry.BUILT_IN` hinzufügen |

## Empfohlener Einstieg

Neue Entwickler in dieser Reihenfolge: **01 → 02 → 07 → 11 → 08**.
Debugging: **07, 08, 11–15**. Erweiterung: **16, 17, 05**.

## Farb-Legende (einheitlich in allen Diagrammen)

| Farbe | Bedeutung |
|-------|-----------|
| Blau | Public API / Einstiegspunkt |
| Violett | Core Engine / zentrale Logik |
| Grün | erfolgreiches Ergebnis |
| Rot | Fehlerpfad |
| Orange | Parsing / Validierung |
| Grau | Hilfsklasse / Utility / extern |
| Gelb | Cache / Konfiguration / Registry |

In Sequenzdiagrammen: durchgezogener Pfeil = Aufruf, gestrichelter Pfeil = Rückgabe/Throw.

## Pflege

Die Diagramme sind manuell gepflegt. Bei Änderungen an den referenzierten Klassen
(`CalculatorEngine`, `Tokenizer`, `PostfixParser`, `Evaluator`, `ExpressionElements`,
`BigNumber`, `BigNumberMatrix`, `BigNumberList`, `UnitConverter`, `UnitRegistry`, …)
das passende Diagramm aktualisieren.
