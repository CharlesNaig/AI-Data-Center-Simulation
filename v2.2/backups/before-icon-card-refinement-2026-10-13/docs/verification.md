# V2.2 verification — 2026-10-13

Model: `v2.2/models/ai-data-center-community-v2.2.nlogox`. Runtime: project-local **NetLogo 7.0.4 / Java 17**. Hypothetical STS teaching assumptions; SDG 6 primary focus.

## Scope implemented

- Separate V2.1 preservation and V2.2 folders; V1/original V2/approved original plan/backups remain unchanged.
- Light gray scene and blue title; larger 1,000 × 440 px View (125 × 55 patches, 8 px), brighter green terrain and winding river. Native controls retain the user's theme.
- Four colored cards with large bitmap numbers, bars, meanings and explicit warnings; actual grid values above 100%; month/year/count/solution footer and artwork legend.
- Direct native Add Solar, Recycle Water and Reforest buttons with ON/OFF indicators; existing Setup/Run/Pause/Step/Build/Statistics behavior retained.
- Original local pixel-art PNGs, deterministic non-overlapping household slots, organized facility compound, protected road/river/bridge/access ground, and vegetation-dependent saplings/mature trees.

## Automated checks

The exact model hash, asset hashes and successful harness output are saved in [verification-results.json](verification-results.json).

- **189 model assertions:** defaults, counts at 1/10/50, separate slot indexes, repeatable setup/layout, unique footprints, all 5 × 5 clearing areas protected, river-channel preservation, facility 51 rejection, pending settings, monthly water balance under all policies, full/empty/spill/zero demand, independent/idempotent interventions, 12-month recovery, occupied-footprint exclusion, vegetation artwork thresholds, score boundaries and duration freeze.
- **Seven BehaviorSpace scenarios / 511 observations:** every water, grid, count, request, allocation-shortfall and balance metric matches preserved V2.1 within 1e-8 absolute / 1e-10 relative tolerance. Green coverage has a new geometry-dependent baseline; scores and categories are recalculated and asserted against the unchanged formula, not forced to match V2.1.
- **Native plots:** 73 initialized/monthly points per pen, actual current values, `ticks / 12` elapsed years and a 100% grid-capacity reference. No predetermined example series.
- **Offline relocation:** model plus all seven sprites/numeric glyphs copied to a folder containing spaces and run while Java network connections are denied. No global network setting was changed.
- **Native GUI:** actual mouse interaction with Setup, Step, Build, Run/Pause, automatic completion, Statistics OK and all three direct solution buttons, including repeat activation. Real NetLogo application screenshots, not mockups.
- **Structural checks:** no overlapping widget rectangles; native chooser/slider binding names; exact count ranges/defaults; View dimensions; valid transparent sprites and all 56 numeric glyphs.

The native GUI harness also checks the **actual rendered button/monitor rectangles**, since NetLogo can enforce a larger minimum size than the XML requests. The direct-action status monitors use that 100 × 60 px minimum with explicit row spacing. Command Center collapse uses NetLogo's own SplitPane, which reclaims the available height without changing app theme preferences.

Reproduce from STS root:

```powershell
rtk proxy "C:\Users\Charles\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" v2.2/tests/verify.py --ui
```

## Conditional month-72 outcomes

| Case | DC/homes | Stock % | Grid % | Green % | Score / category |
| --- | --- | ---: | ---: | ---: | --- |
| Baseline | 1/10 | 98.37 | 2.85 | 99.34 | 98.29 / Healthy |
| Expansion | 20/10 | 0 | 56.05 | 86.82 | 39.23 / Stressed |
| Recycled water | 20/10 | 87.03 | 56.05 | 86.82 | 74.04 / Moderate |
| Solar | 20/10 | 0 | 33.65 | 86.82 | 45.95 / Stressed |
| Reforestation | 20/10 | 0 | 56.05 | 95.26 | 41.76 / Stressed |
| Combined | 20/10 | 87.03 | 33.65 | 95.26 | 83.29 / Healthy |
| Maximum + combined | 50/50 | 0 | 84.25 | 86.90 | 30.79 / Stressed |

Exact final observations: [scenario-summary.csv](scenario-summary.csv). Expansion builds 19 additional facilities after month 12. Individual/combined cases start with 20 facilities and activate after month 24; maximum starts at 50/50 and also activates after month 24. The maximum case starts at 140.25% grid demand and retains **5.35 ML/month water shortfall** after all solutions, demonstrating conditional recovery.

## Native visual evidence

Application frames are captured at **1366 × 768** and **1920 × 1080**, with Command Center collapsed. These are window sizes; OS resolution was not changed. Main presentation fits at the smaller size; graphs/assumptions are available by scrolling. At the larger size all plots fit below the scene. Labels, sprites, protected footprints, bars, warnings and footer are inspected for overlap and clipping.

| State | 1366 × 768 | 1920 × 1080 |
| --- | --- | --- |
| Setup | [Image](screenshots/setup-1366x768.png) | [Image](screenshots/setup-1920x1080.png) |
| Shortage, month 24 | [Image](screenshots/shortage-1366x768.png) | [Image](screenshots/shortage-1920x1080.png) |
| Saplings, month 26 | [Image](screenshots/saplings-1366x768.png) | [Image](screenshots/saplings-1920x1080.png) |
| Recovery, month 36 | [Image](screenshots/recovery-1366x768.png) | [Image](screenshots/recovery-1920x1080.png) |
| Completion, month 72 | [Image](screenshots/completion-1366x768.png) | [Image](screenshots/completion-1920x1080.png) |
| Maximum 50/50 | [Image](screenshots/maximum-1366x768.png) | [Image](screenshots/maximum-1920x1080.png) |

Other evidence: [graphs](screenshots/charts-1366x768.png), [grid above 100%](screenshots/maximum-charts-1366x768.png), [grayscale warning](screenshots/shortage-grayscale-1366x768.png), [Statistics](screenshots/statistics-dialog.png).

Measured WCAG sRGB contrast for the model-owned text palette: ordinary dark text on white **16.29:1**, title **11.15:1**, terrain caption **9.12:1**, and blue/amber/green/red value colors on white **6.69 / 5.34 / 7.38 / 7.28:1**. Patch labels are 16 px, large numeric glyphs use 26 px bold type, and native widget surfaces retain the current application theme. All explicit model palette text colors pass 4.5:1.

## Preservation and limitations

V2.1's source model/assets were copied byte-for-byte and hash checked; the original copied-file record is in `../v2.1/docs/copied-file-hashes.json`. Its 176 model assertions, 511-observation regression and offline relocation passed in the separate folder. Opening V2.1 reverses the design choice without overwriting any file. Existing V2/V1/docs/tests/results are protected by before/after SHA-256 checks during this task. New files are limited to version folders plus root README links and scoped Obsidian entries.

Native button chrome follows NetLogo's theme; it does not reproduce the reference's illustrated buttons exactly. Only NetLogo 7.0.4 on the present Windows setup was validated; alternate OS/theme/scaling and NetLogo 6 export are unchecked. No dependency, global Python repair, commit, push or application-theme change was made. Group names and instructor version acceptance remain submission tasks.
