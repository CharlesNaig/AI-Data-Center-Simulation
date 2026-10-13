# STS AI Data Center Community — V2.2

Native **NetLogo 7.0.4** classroom simulation with a light pixel-art dashboard, larger landscape, winding river, direct solution buttons and colored live indicators. SDG 6 is the primary focus. Every default and well-being threshold is an illustrative teaching choice, not a measurement or forecast of actual PAX Silica infrastructure.

## Open and run

1. Open [`models/ai-data-center-community-v2.2.nlogox`](models/ai-data-center-community-v2.2.nlogox) in NetLogo 7.0.4.
2. Select **starting-data-centers** and **starting-households**, then **Setup / Reset**. Defaults: one facility and ten households; both ranges: 1–50.
3. Use **Build Data Center +1**, **Step Month** or **Run / Pause**.
4. Enable **Add Solar**, **Recycle Water** or **Reforest** directly. Their adjacent ON/OFF monitors and the footer show active solutions. Repeat clicks never stack benefits.
5. Open **Statistics** for current requests and last-month allocation/recharge/spill/shortfall. Native statistics dialogs pause an active run; OK resumes it and Halt stops it.
6. Results freeze at month 72 by default. **Setup / Reset** clears history and solutions and applies all selected settings.

Keep the entire `v2.2/` folder together when sharing. PNGs and numeric glyphs load relative to the model. NetLogo's Bitmap extension is bundled; no network connection is required to run. At 1366 × 768, collapse Command Center via **Tabs → Command Center** or drag its divider to the bottom. The main presentation fits; scroll down for all graphs and advanced assumptions. At 1920 × 1080 the main scene and graphs fit with Command Center collapsed. Native controls and application chrome retain the user's NetLogo theme; the model-owned scene, title and explanation panels use the light palette.

## Choose between versions

- **V2.2:** this folder, the latest light pixel-art dashboard and rebuilt land geometry.
- **V2.1:** [`../v2.1/`](../v2.1/README.md), an independently runnable preserved copy of the natural-layout version. Open its model to compare or return to that design; no restore or overwrite is needed.
- **Before the icon/card refinement:** [Open the saved V2.2 model](backups/before-icon-card-refinement-2026-10-13/models/ai-data-center-community-v2.2.nlogox) to compare or use the prior look. The backup includes its own relative-path assets and a SHA-256 manifest; opening it does not change the current V2.2 files. [Backup notes](docs/icon-refinement-backup.md).
- **Original V2:** `../v2/` remains unchanged, including its historical backups. V1 and `PLAN-sts-v2.md` are also preserved.

V2.1's model/assets were copied byte-for-byte and hash checked before adapting its README and test paths. Temporary results and historical backup folders were excluded from the version copies.

## Indicators and plots

| Indicator | Meaning |
| --- | --- |
| Water | Reservoir stock / capacity, also shown as ML; distinct from monthly water service |
| Grid | Actual average requested grid demand / modeled capacity; readings above 100% remain visible |
| Green cover | Current vegetation / baseline recorded after households and before facilities |
| Well-being | Illustrative weighted score / 100, with its condition category |

Water, grid, green and well-being cards use blue, amber, green and red respectively. Card titles and short status lines are centered; large values use proportional-width glyphs. Red identifies the well-being panel, not an automatic unhealthy verdict. Bars fill within their graphical bounds; **Exceeds capacity** and **Water shortfall** give explicit warnings, including in grayscale. No blackout or automatic facility shutdown is modeled.

`Score = 0.40 × water stock % + 0.30 × max(0, 100 − grid demand %) + 0.30 × green cover %`

Healthy ≥75; Moderate ≥50 and <75; Stressed ≥25 and <50; Critical <25. This is neither measured human health nor a fairness metric.

One tick = 30 days; charts use `ticks / 12` elapsed years. They contain initialized state plus real completed monthly observations. Building or enabling a solution updates prospective requests and current indicators without advancing time, withdrawing another month's water or inserting duplicate chart points. Last-month balances remain historical until the next step.

## Resource model and land

Defaults: 1 MW/facility, 70% IT utilization, PUE 1.6, WUE 1 L/IT kWh, 500 L/day and 2 kW per home, 40 ML reservoir, 8 ML monthly recharge, 40 MW grid, proportional allocation and 72-month duration. All advanced controls apply with Setup.

- IT energy = facilities × capacity × utilization × 720,000 kWh/month.
- Site water = IT energy × WUE / 1,000,000 ML/month.
- Facility freshwater = site water × (1 − recycled share).
- Household water = households × L/day × 30 / 1,000,000 ML/month.
- Grid % = 100 × (facilities × capacity × utilization × PUE × (1 − solar share) + households × kW/1000) / grid MW.

Default requests remain **0.504 ML/month** per facility, **0.15 ML/month** for ten homes and **2.85%** combined grid demand. Recharge precedes allocation; excess spills. Proportional, community-first and data-center-first policies conserve water while distributing scarce supply differently.

The 125 × 55 world has a protected landscape at x=1–93, y=5–51. River center is `43 + 5 × sin((y − 5) × 7)` with channel half-width 3. Village/facility roads at x=35/55, connecting road at y=7, and the bridge at y=26 remain protected. Dry river patches cannot become building slots.

Fifty shuffled, non-overlapping household slots occupy x=3–32, y=10–46. Fifty facility slots occupy seven columns at `x=59+5×column`, `y=46−5×row`. Slot indexes are independent of turtle IDs. Each facility occupies 3 × 3 patches and clears a 5 × 5 area; every clearing is checked against protected terrain.

Recycled water replaces 50% of facility freshwater. Solar supplies 40% of facility electricity without changing water. Reforestation restores 1/12 of baseline vegetation per month on eligible unoccupied land without changing recharge. Pixel saplings appear at vegetation ≥0.10 and <0.75; mature trees at ≥0.75, only when their artwork avoids building sprites and protected/occupied ground. New clearing starts its own 12-month recovery. Facility sprites are larger for small communities and shrink at high counts; the first buildings form a close compound, with solar shown beside it. Wildlife is limited habitat artwork; decoration adds no agents.

The rebuilt terrain changes green-cover baselines and therefore scores; **water/grid/count/allocation outputs match V2.1 across all 511 scenario observations**. Recovery is conditional: the 50/50 case still has 5.35 ML/month shortfall after all solutions. CO₂, pollution, waste heat, e-waste, disease, jobs, costs, independent population health and detailed hydrology remain outside the model.

## Verification

From the STS root:

```powershell
rtk proxy "C:\Users\Charles\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" v2.2/tests/verify.py --ui
```

A working Python 3 interpreter with its standard library is sufficient. The verifier uses the project-local runtime; set `NETLOGO_HOME` to a NetLogo 7.0.4 installation if needed. `--ui` opens, tests, captures and closes its own native application. Omit it for numeric, structural, scenario and offline checks.

The seven built-in BehaviorSpace scenarios record month 0 through 72: baseline; expansion by 19 facilities after month 12; recycled-water, solar and reforestation individually; combined; maximum 50/50. Intervention cases enable their solutions after month 24.

See [verification and screenshots](docs/verification.md), [scenario summary](docs/scenario-summary.csv) and [machine-readable evidence](docs/verification-results.json). Full generated traces are under ignored `results/`.

## Artwork and limitations

[Original pixel-art asset credits and prompts](assets/CREDITS.md). The seven sprites were generated using the built-in image tool, saved locally and scaled through Bitmap. Numeric glyph PNGs use Java's standard drawing library for large colored values; they require no runtime font service. No new dependency or custom controls were introduced.

Only NetLogo 7.0.4 on the current Windows setup is validated. Native button colors depend on the application's theme; this is a close interpretation of the reference, not an identical custom skin. Add group names to Info and confirm the instructor accepts `.nlogox` before submission.
