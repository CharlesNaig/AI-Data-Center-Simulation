# Simulation View verification — 2026-10-02

Target: NetLogo 7.0.4. This is a schematic teaching model, not a geographic layout or PAX Silica forecast.

## Implemented

- Approximately 1,000 × 575 px presentation area; unchanged world bounds, 18 px patches, 16 px View labels.
- A fixed reservoir container with proportional blue fill and percentage, two static supply arrows, one teal aggregate community, and 30 purple server positions in six columns/five rows.
- ID-ordered facility captions; persistent sector identity colors; amber outlines and explicit Water shortfall text in the View and monitors.
- Service status uses the existing 99.999% tolerance. Three-decimal service percentages distinguish 99.998% from the threshold. Month-zero service/shortfall displays are Ready—advance one month.
- Native Load preset, Reset, forever Run / Pause, and single-month Step buttons; original commands unchanged. Settings and original plots are below the main area.
- ML and service-percent definitions, preset reset warning, and compact legend. Small floating-point stock residuals are rounded only for display.

## Numeric and structural checks

Fresh pre-edit CSVs were captured for all four experiments, then rerun after the redesign:

| Experiment | Completed runs |
| --- | ---: |
| smoke-test | 1 |
| scenario-comparison | 3 |
| rainfall-uncertainty | 30 |
| allocation-policy-check | 3 |

All 37 final rows match by scenario/policy/seed within absolute tolerance 1e-6 and relative tolerance 1e-10. Formula/allocation self-tests print SELF-TESTS PASSED. World boundaries, slider values/ranges/defaults, plot content, and experiment definitions are unchanged; widget rectangles do not overlap.

Original placement random draws are deliberately retained before fixed placement. Original update asks remain; added visual asks/drawing run in with-local-randomness. No decorative agents were added: the 30-facility test contains exactly 32 turtles and 30 unique facility positions.

## Visual and edge checks

Native NetLogo application renders were inspected for setup and month 120 of all three presets, plus 30 facilities, at window sizes 1366 × 768 and 1920 × 1080. The Command Center is hidden for classroom presentation at the smaller size. This verifies application-window dimensions, not a change to the computer's display resolution. Settings/charts remain available by vertical scrolling.

Headless View exports and assertions cover full/empty reservoir, all allocation policies, zero freshwater demand under every policy, the Ready state, and the 99.999/99.998 status boundary. Captions, buildings, arrows, and result boxes were checked for overlap. Native screenshots retain full sector service/shortfall text; standalone View exports also contain Water shortfall text, so shortages do not depend on color discrimination.

WCAG sRGB text contrast against #F8FAFC: slate 17.06:1, blue 4.96:1, teal 5.08:1, purple 6.79:1, amber 4.80:1. Native widgets retain NetLogo's theme styling; View/note surfaces use explicit colors for readable labels in the tested dark application theme.

The Windows automation helper could not reliably target two same-named NetLogo executables. Verification instead used the documented [NetLogo Controlling API](https://github.com/NetLogo/NetLogo/wiki/Controlling-API), loading the full NetLogo application and rendering its actual frame. No mock interface was substituted. The application harness redraws the scene after resizing before capturing it; its extra redraws are not used for numeric regression comparisons.

NetLogo 7's GUI derives chooser variable bindings from display names. The policy chooser therefore retains water-allocation-policy, rather than a humanized display name that compiled headlessly but failed in the actual GUI. The regression check now guards this distinction.

## Saved screenshots

- [Baseline, 1366 × 768](screenshots/baseline-1366x768.png)
- [Shortage, 1366 × 768](screenshots/shortage-1366x768.png)
- [Baseline, 1920 × 1080](screenshots/baseline-1920x1080.png)
- [Shortage, 1920 × 1080](screenshots/shortage-1920x1080.png)
- [30 facilities, 1366 × 768](screenshots/30-facilities-1366x768.png)

## Reproduce

Run from the project root. CSVs and intermediate PNGs in results are ignored by Git; selected screenshots above are retained.

```powershell
$runtime = Join-Path $PWD '.tools/netlogo-7.0.4/PFiles64/NetLogo 7.0.4'
$model = Join-Path $PWD 'models/ai-data-center-water-energy.nlogox'

# Capture ui-before-*.csv BEFORE editing; ui-after-*.csv AFTER editing.
foreach ($experiment in 'smoke-test', 'scenario-comparison', 'rainfall-uncertainty', 'allocation-policy-check') {
  & "$runtime/NetLogo_Console.exe" --headless --model $model --experiment $experiment --table "$PWD/results/ui-after-$experiment.csv"
  if ($LASTEXITCODE -ne 0) { throw "Experiment failed: $experiment" }
}
rtk python tests/verify-ui.py

# Edge assertions and View exports (Java source launcher; no external test framework).
& "$runtime/runtime/bin/java.exe" '-Dfile.encoding=UTF-8' --class-path "$runtime/app/*" tests/UiViews.java $model

# Full application renders; closes only its own test application when finished.
& "$runtime/runtime/bin/java.exe" '-Dfile.encoding=UTF-8' "-Dnetlogo.docs.dir=$runtime" "-Dnetlogo.extensions.dir=$runtime/extensions" "-Dnetlogo.models.dir=$runtime/models" --class-path "$runtime/app/*" tests/UiApp.java $model "$PWD/results"
```

The visual check uses a testing-only initial value of 30 to populate all slots; the actual initial-facility slider remains 1–10. A user can also reach 30 through growth with maximum-data-centers set to 30.
