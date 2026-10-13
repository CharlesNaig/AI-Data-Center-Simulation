# AI Data Center Community Simulation — V2

A native **NetLogo 7.0.4** classroom model about shared water, electricity demand, land clearing and recovery. SDG 6 is the primary focus. All numbers are hypothetical teaching assumptions; this is **not a claim or forecast about actual PAX Silica infrastructure**.

## Open and run

1. Open [`models/ai-data-center-community-v2.nlogox`](models/ai-data-center-community-v2.nlogox) in NetLogo 7.0.4.
2. Set **starting-data-centers** and **starting-households**, then click **Setup / Reset**. Defaults are one facility and ten households; both ranges are 1–50.
3. Use **Build More +1** to construct a facility. Use **Step Month** or **Run / Pause** to advance time.
4. Open **Statistics** for precise requests, allocations and the last month's balance. Open **Sustainable Solutions** to enable recycled water, solar, or reforestation. Repeat choices never stack.
5. Results freeze after 72 monthly steps by default. **Setup / Reset** starts over, clears history, and turns solutions off.

One tick is 30 days. The graphs show elapsed years (`ticks / 12`), with actual monthly observations. A month-zero point represents the initialized state. Building and enabling solutions immediately update current requests and land/grid indicators, without an extra withdrawal or a duplicate chart point. Last-month supply and shortfall remain historical until the next step.

Keep the **whole `v2/` folder together** when copying/sharing. The model loads `../assets/*.png` relative to its own folder, with no internet connection required. Bitmap is already bundled with NetLogo. The repository's local runtime is at `../.tools/netlogo-7.0.4/PFiles64/NetLogo 7.0.4/` relative to `v2/`.

At **1366 × 768**, hide/collapse Command Center via **Tabs → Command Center**. The entire main landscape, controls and indicators fit; scroll down for the full graphs and assumptions. At **1920 × 1080**, the graphs also fit when Command Center is collapsed. This is a fixed-width native NetLogo interface.

Native dialogs pause Run while open. OK resumes it if it was running; Cancel/Halt stops it. No persistent custom pause flag is used. For a deliberate comparison, pause before opening Statistics or choosing a solution.

## What the indicators mean

| Indicator | Meaning |
| --- | --- |
| Water Supply | Reservoir stock / capacity, also shown in ML (million liters). This differs from the share of monthly demand supplied. |
| Grid Demand | Average requested grid MW / modeled capacity. Values above 100% remain visible; no blackouts or automatic shutdowns are simulated. |
| Green Coverage | Vegetation remaining / baseline measured after homes, before initial facilities. |
| Community Well-Being | Illustrative weighted score below; not measured health or fairness. |

`Score = 0.40 × water stock % + 0.30 × max(0, 100 − grid demand %) + 0.30 × green coverage %`

Healthy ≥75; Moderate ≥50 and <75; Stressed ≥25 and <50; Critical <25. Weights and thresholds are explicit teaching choices. A high starting facility count can start below Healthy.

Blue identifies water, amber grid demand, green vegetation, and purple the facility zone. Shortages also show **Water shortfall** and an outlined water panel; grid overload has explicit **Exceeds capacity** text. Warnings remain understandable in grayscale.

## Assumptions and equations

All advanced inputs are below the graphs. **All edits apply with Setup**, including allocation policy and duration; the Settings monitor identifies pending changes. Snapshotting settings prevents mid-month changes and preserves the 72-month freeze.

| Assumption | Default |
| --- | ---: |
| Facility IT capacity; utilization | 1 MW; 70% |
| PUE; WUE | 1.6; 1 L / IT kWh |
| Per household water; average power | 500 L/day; 2 kW |
| Reservoir capacity; monthly recharge | 40 ML; 8 ML |
| Grid capacity | 40 MW |
| Allocation | proportional |
| Run duration | 72 months |

- IT energy = facilities × capacity × utilization × 720,000 kWh/month.
- Site water = IT energy × WUE / 1,000,000 ML/month.
- Facility freshwater = site water × (1 − recycled share).
- Household water = households × liters/day × 30 / 1,000,000 ML/month.
- Grid demand % = 100 × (facilities × capacity × utilization × PUE × (1 − solar share) + households × kW/1000) / grid capacity.

One default facility requests **0.504 ML/month**, ten homes request **0.15 ML/month**, and their combined grid demand is **2.85%**. PUE changes electricity; it does not multiply WUE water use.

Each month: calculate requests → recharge/spill → allocate available water → calculate grid demand → restore eligible vegetation → score → advance/plot. The balance is `previous stock + recharge = spill + household supply + facility supply + ending stock`. Proportional, community-first and data-center-first allocation redistribute scarce water without creating it.

## Construction and solutions

Homes and facilities have independent slot indexes and 50 fixed positions each. A facility occupies nine unique patches (3 × 3) and clears its 5 × 5 surrounding area. River, roads, homes and the display sidebar remain protected. A dry river does not become buildable. House count changes both water and power demand.

| Solution | Effect | Boundary |
| --- | --- | --- |
| Recycled water | Replaces 50% of facility site water with reclaimed water | Household demand and total site water remain unchanged |
| Solar energy | Supplies 40% of facility electricity | No direct effect on water or vegetation |
| Reforestation | Restores 1/12 of baseline vegetation per month on eligible cleared ground | Never restores buildings, homes, river or roads; does not increase recharge |

Reforestation persists: if later construction clears more eligible ground, that ground takes its own 12 months to recover. Fractional patch vegetation supplies gradual recovery; tree/wildlife artwork follows vegetation thresholds. Wildlife is a habitat illustration, not a simulated population.

Recovery is not guaranteed. CO₂, pollution, waste heat, e-waste, disease, jobs, costs, independent population health and detailed hydrology are outside V2's scope.

## Reproduce the verification

From the STS project root, with a working Python 3 interpreter:

```powershell
rtk proxy python v2/tests/verify.py --ui
```

The checks use the project-local NetLogo runtime. Set `NETLOGO_HOME` to another NetLogo 7.0.4 installation folder if needed. `--ui` opens a temporary native test application, exercises its buttons/dialogs, saves screenshots, and closes only that test process. Omit it for numeric/structural/BehaviorSpace/offline checks. No Python packages are required.

On this machine the bundled Python works even if the default installation reports an `encodings` error:

```powershell
rtk proxy "C:\Users\Charles\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" v2/tests/verify.py --ui
```

The built-in **v2-scenarios** BehaviorSpace experiment covers baseline, expansion, each solution individually, combined solutions, and 50 facilities/50 households. The expansion case adds 19 facilities after month 12. Intervention cases start with 20 facilities and enable their named solution(s) after month 24. The maximum case enables all three after month 24. Each records month 0 through 72.

See [verification and screenshots](docs/verification.md), [final scenario values](docs/scenario-summary.csv), and [machine-readable evidence](docs/verification-results.json). Full generated traces and plot exports remain in `results/`, ignored by Git.

## Credits and scope

Artwork by [Icons8](https://icons8.com), Fluency family; [asset credits and sources](assets/CREDITS.md). It is bundled as part of the model, not as a standalone icon pack. [NetLogo Bitmap documentation](https://docs.netlogo.org/7.0.4/bitmap.html) describes the bundled rendering extension.

The original model at `../models/ai-data-center-water-energy.nlogox`, its tests and numerical results are preserved. V2 deliberately changes the simulation scope, so its outputs are not numerically equivalent to V1. Add group names in Info and confirm the instructor accepts NetLogo 7 `.nlogox` before submission; no NetLogo 6 export was tested.
