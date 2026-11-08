# Preserved V2.1 verification

Copied from the latest natural-layout V2. Its 176 model assertions, resource regression, plot checks and offline relocation were rerun successfully in this separate folder. The native screenshots below are the preserved original evidence for the identical model/assets. Copied source hashes are retained in copied-file-hashes.json; README and verifier paths are adapted, not the model equations or artwork.

# V2 verification — 2026-10-13

Model: `v2/models/ai-data-center-community-v2.nlogox`, tested with the project-local **NetLogo 7.0.4 / Java 17** runtime. This is a hypothetical STS teaching model, with SDG 6 as its primary focus; outcomes are conditional on the documented assumptions.

## Automated verification

Run from the STS root:

```powershell
rtk proxy "C:\Users\Charles\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" v2.1/tests/verify.py --ui
```

The verifier requires only Python's standard library and the existing NetLogo runtime. `NETLOGO_HOME` can point to another NetLogo 7.0.4 installation. It runs:

- **176 numeric/state assertions:** default equations, both counts at 1/10/50, exactly one turtle per home/facility, independent slot indexes, unique positions, nine-patch facility/home footprints, protected land, rejection of facility 51, pending slider edits, deterministic Reset and natural layout, irregular tree positions, construction without extra withdrawal/tick, historical allocation preservation, individual solution isolation/idempotence, 12-month eligible recovery, later-construction recovery, score boundaries at 25/50/75, all three water policies, full/empty stock, spill, zero request, and the 72-month freeze.
- **Seven BehaviorSpace cases / 511 observations:** baseline, manual expansion, recycled water, solar, reforestation, combined interventions, and 50 facilities/50 households. Each records month 0 through 72. Balance error is within 1e-8 ML throughout. Separate V2 outputs do not assert equality with V1.
- **Native plot exports:** 73 initialized/monthly points per pen, x = ticks / 12, actual default water/grid/green values and a 100% grid capacity reference. No example series is hardcoded. Grid plots allow values above 100%.
- **Offline relocation:** copies only the model and assets to a path containing spaces, denies Java network connections, then loads Bitmap PNGs and runs all three solutions. This passes using NetLogo's bundled extension. No machine-wide network setting is changed.
- **Native GUI interaction:** actual NetLogo Setup, Step, Build More, Run/Pause, automatic completion, Statistics OK, Solutions Cancel, each of the three dropdown choices and repeated choices. Statistics and Cancel preserve resource state/time; enabling a solution never advances ticks or withdraws water. The test uses the real native widgets and dialogs, not a mock dashboard.
- **Structural checks:** no overlapping widget rectangles; chooser/slider display names preserve native variable bindings; counts default to 1/10 and range 1–50; View is 125 × 57 patches at 8 px (1,000 × 456 content, plus native border); all six assets are valid 48 × 48 local PNGs.

Machine-readable results and SHA-256 hashes of the model/assets are in [verification-results.json](verification-results.json). Final observations are retained in [scenario-summary.csv](scenario-summary.csv). Full monthly traces and baseline plot exports are generated under `v2/results/` and ignored by Git.

## Conditional final outcomes

| Case at month 72 | DC / homes | Water stock % | Grid % | Green % | Score / category |
| --- | --- | ---: | ---: | ---: | --- |
| Baseline | 1 / 10 | 98.37 | 2.85 | 99.21 | 98.25 / Healthy |
| Expansion | 20 / 10 | 0 | 56.05 | 84.10 | 38.42 / Stressed |
| Recycled water | 20 / 10 | 87.03 | 56.05 | 84.10 | 73.23 / Moderate |
| Solar | 20 / 10 | 0 | 33.65 | 84.10 | 45.14 / Stressed |
| Reforestation | 20 / 10 | 0 | 56.05 | 94.28 | 41.47 / Stressed |
| Combined | 20 / 10 | 87.03 | 33.65 | 94.28 | 83.00 / Healthy |
| Maximum, combined | 50 / 50 | 0 | 84.25 | 83.84 | 29.88 / Stressed |

Expansion builds 19 additional facilities after month 12. Individual and combined cases start with 20 facilities and enable solutions after month 24. Maximum starts with 50 facilities/50 households and enables all three after month 24. Under the maximum case, freshwater requests remain 13.35 ML/month against 8 ML recharge, leaving **5.35 ML/month shortfall** despite all interventions. This demonstrates that the model does not guarantee recovery.

The default 0.504 ML facility request, 0.15 ML household request and 2.85% grid demand were checked directly. At 50/50 before solar the grid reads **140.25%**. Water stock % is separate from monthly service; the score uses stock, grid headroom and baseline vegetation, not a measured health or fairness outcome.

## Native visual review

Actual NetLogo application-frame images were captured and inspected at **1366 × 768** and **1920 × 1080** for setup, shortage, recovery, completion and maximum counts. These are application-window sizes; the computer's display resolution was not changed. Command Center is collapsed for presentation. At 1366 × 768 the full main controls/landscape/indicators fit; graphs and advanced settings are reached by vertical scrolling. At 1920 × 1080 the main scene and all three graphs fit.

Trees, buildings, homes, river and captions were checked for overlap. Warning text stays readable independent of sector colors, including a grayscale shortage capture. The river's protected channel remains visible when stock reaches zero. Recovery never paints trees underneath occupied footprints. Month, elapsed years, counts, active solutions, legend and units remain visible; completion displays frozen status.

| State | 1366 × 768 | 1920 × 1080 |
| --- | --- | --- |
| Setup | [Screenshot](screenshots/setup-1366x768.png) | [Screenshot](screenshots/setup-1920x1080.png) |
| Shortage, month 24 | [Screenshot](screenshots/shortage-1366x768.png) | [Screenshot](screenshots/shortage-1920x1080.png) |
| Recovery, month 36 | [Screenshot](screenshots/recovery-1366x768.png) | [Screenshot](screenshots/recovery-1920x1080.png) |
| Completion, month 72 | [Screenshot](screenshots/completion-1366x768.png) | [Screenshot](screenshots/completion-1920x1080.png) |
| 50 facilities / 50 households | [Screenshot](screenshots/maximum-1366x768.png) | [Screenshot](screenshots/maximum-1920x1080.png) |

Additional evidence: [full graphs at 1366](screenshots/charts-1366x768.png), [grid above 100%](screenshots/maximum-charts-1366x768.png), [grayscale shortage](screenshots/shortage-grayscale-1366x768.png), [Statistics dialog](screenshots/statistics-dialog.png), [Solutions dialog](screenshots/solutions-dialog.png).

View labels are 16 px. WCAG sRGB contrast of labels against their specified surfaces: landscape dark text **13.62:1**, purple facility caption **7.29:1**, pale sidebar text **13.62:1**, blue/amber/green/purple sidebar headings **8.95 / 10.35 / 10.63 / 8.44:1**. Native widgets retain the application's own theme; these measurements apply to the model's explicit scene text palette.

## Implementation decisions and fixes found during verification

### Natural arrangement and revert backup — 2026-10-13

Charles requested less uniform trees and houses, with a revert backup for the leader's design review. Trees now use irregular seeded groves, varied crown sizes and clearings; houses use shuffled positions, small offsets and three artwork sizes. Data centers retain the organized compound. The seed is local to layout generation, so Reset reproduces the arrangement without changing resource randomness. Trees are drawn only where the whole crown bounds avoid protected, occupied or insufficiently vegetated ground. Home footprints use rounded centers and remain nine patches each.

All **511 monthly observations** were compared with the saved original trace and match within 1e-8 absolute / 1e-10 relative tolerance, including vegetation and score. Updated native screenshots at both target sizes were inspected for sparse/default and dense/maximum placement, shortage and recovery.

The original model, assets, tests, docs and screenshots are saved in [the pre-change backup](../../v2/backups/2026-10-13-before-natural-layout/README.md). Its restore script was exercised on a temporary copy: all **32 snapshot files** restored byte-for-byte; the replaced version and an unrelated file were preserved. Restoring the old layout saves the current version first and does not change V1 or Obsidian. The saved snapshot and manifest are kept unchanged.

- All sliders and the allocation chooser are captured at Setup. The Settings monitor detects pending edits. This also prevents changing duration from unfreezing completed results.
- The default plot point is the initialized state. Subsequent points come only from complete monthly updates. Construction and solution activation update current demand/land/grid immediately, without inserting duplicate monthly points.
- Native dialogs already pause forever buttons. An extra custom pause flag was removed after real GUI Cancel testing showed that NetLogo halts the calling procedure, preventing its cleanup code from running. Cancel/Halt stops Run; the model can subsequently Step or Run without Reset. [NetLogo user-interaction documentation](https://docs.netlogo.org/programming) describes this native behavior.
- Native dropdowns and forever buttons are exercised through their own selection/mouse behavior. Headless compilation alone cannot validate GUI variable bindings, cancellation, or Run/Pause.
- Cached bitmap artwork adds no decorative agents. Separate patch state stores protected terrain and vegetation; facility and home slots do not depend on turtle IDs.
- Icons8 credit is in Info, the interface, README and [asset credits](../assets/CREDITS.md), following [Icons8 guidance](https://icons8.com/license). [Bitmap](https://docs.netlogo.org/7.0.4/bitmap.html) is supplied by NetLogo.

## Preservation and remaining limits

The legacy model, tests, results and approved `PLAN-sts-v2.md` were not edited by this implementation. The root README receives only a V2 link; all substantive new files are under `v2/`. Existing dirty work was preserved. Existing Graphify data was reused as legacy context, not rebuilt or presented as an index of V2. No commit or push was made.

Only NetLogo 7.0.4 was validated. No NetLogo 6 `.nlogo` export or alternate theme, operating system or display scaling was tested. The offline assertion uses the Java 17 test-only SecurityManager supported by this runtime. Group names and instructor version acceptance remain submission tasks. Assumptions, score weights and categories are educational choices, not empirical infrastructure calibration.
