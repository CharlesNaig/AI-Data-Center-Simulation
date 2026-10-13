# AI Data Center Community Simulation — V2 Plan

## 1. Summary and agreed direction

Build a classroom-friendly version following the designers’ landscape concept, with **SDG 6 as the primary focus**.

Confirmed choices:

- **Monthly ticks:** one step equals 30 days; charts display elapsed years.
- **Data centers:** starting-count slider from 1–50, default 1; Build More adds one facility per click.
- **Community:** adjustable households; one house represents one household.
- **Final result:** an explicitly illustrative 0–100 well-being score with Healthy, Moderate, Stressed, and Critical categories.

Create `models/ai-data-center-community-v2.nlogox`. Preserve the existing model and its verified results as the legacy version. V2 intentionally changes behavior; its outputs must not be presented as equivalent to the previous experiments.

Retain neutral framing: outcomes follow stated assumptions, and sustainable measures improve particular pressures without guaranteeing complete recovery.

## 2. UI-first design

Use native NetLogo 7.0.4, not a separate website. Implement and review the complete layout before connecting the new calculations.

### Main presentation

Arrange an approximately **1,220 × 575 px** area:

```text
[Controls and starting counts] [Community–river–forest–data centers] [Live indicators]
                              [Month/year, counts, active solutions, legend]

[Statistics monitors and three colored graphs below]
[Advanced assumptions and explanation below]
```

- Use a dark-framed dashboard with a light landscape, matching the designers’ direction. Preserve native widget styling rather than changing the user’s application theme.
- Use a 1,000 × 456 px View: a 760 px landscape plus a 240 px indicator sidebar. Sidebar patches are display-only and excluded from land calculations.
- Provide 50 fixed household positions and 50 fixed facility positions. Use separate slot indexes—not turtle IDs—so changing household count cannot disrupt facility placement.
- Draw recognizable houses, server buildings, trees, a protected river channel, and limited wildlife illustrations. Avoid labeling every building; show group totals prominently.
- Right-side indicators: Water Supply, Grid Demand, Green Coverage, and Community Well-Being. Each includes its value, meaning, and explicit warning/status text.
- Display water quantity as both reservoir percentage and ML. Explain **ML = million liters**.
- Use blue for water, amber for grid demand, green for vegetation, and purple for data centers. Keep sector colors stable during shortages; add warning text and outlines.
- Target 16 px View labels, clearly labeled controls, and at least 4.5:1 text contrast. Verify both 1366 × 768 and 1920 × 1080; hide/collapse Command Center for the smaller presentation window.

### Icons and charts

Use one consistent **Icons8 Fluency** family for houses, servers, trees, wildlife, recycled water, and solar energy. Cache appropriately scaled PNGs locally and render through NetLogo’s bundled [Bitmap extension](https://docs.netlogo.org/7.0.4/bitmap.html); icons remain artwork, not additional simulation agents.

Include Icons8 credit in the Info tab and README according to its [attribution guidance](https://icons8.com/license). Do not distribute a standalone icon pack.

Provide three native line plots:

- Water Supply — blue, 0–100%.
- Grid Demand — amber; allow values above 100%, with a capacity reference at 100%.
- Green Coverage — green, 0–100%.

Plot actual monthly results against `ticks / 12`; never reproduce the sample chart values as predetermined outcomes.

## 3. Controls and understandable flow

Use these controls:

| Control | Behavior |
|---|---|
| Starting data centers | 1–50; applied by Setup/Reset |
| Households | 1–50, default 10; applied by Setup/Reset |
| Setup / Reset | Rebuild using selected counts; clear history and deactivate solutions |
| Run / Pause | Native forever button |
| Step Month | Complete exactly one monthly update |
| Build More +1 | Add one facility, stopping at 50 |
| Statistics | Open a native numerical summary; graphs remain directly below the main presentation |
| Sustainable Solutions | Open a native selection dialog to enable an intervention |

Because Setup already creates at least one facility, omit the redundant “Build First Data Center” button.

The presentation flow is:

```text
Setup → observe starting community → build more → advance months
      → inspect statistics → activate solutions → observe recovery
      → explain the final condition
```

- Starting sliders do not silently alter a running simulation; show “Apply starting-count changes with Setup.”
- Construction changes land occupation and current demand immediately, but does not advance time or withdraw another month’s water.
- Commit construction and intervention changes between complete monthly updates.
- Repeating an enabled intervention must not stack its benefit.
- Default duration: six years, or 72 monthly steps. At completion, freeze results until Setup/Reset.
- Keep statistics viewing separate from interventions; opening either dialog must not change time or resource balances.

## 4. Simulation rules and illustrative defaults

Reuse the existing WUE/PUE conversion and water-allocation helpers where compatible. Remove automatic facility growth from V2: expansion is user-controlled.

Use these transparent teaching defaults, not claims about Philippine infrastructure:

| Assumption | Default |
|---|---:|
| IT capacity per facility | 1 MW |
| IT utilization | 70% |
| PUE / WUE | 1.6 / 1 L per IT kWh |
| Household water demand | 500 L/day |
| Household average electricity demand | 2 kW |
| Reservoir capacity / monthly recharge | 40 ML / 8 ML |
| Local grid capacity | 40 MW |
| Recycled-water intervention | 50% of facility site water |
| Solar intervention | 40% of facility electricity |
| Reforestation recovery | Full eligible vegetation recovery over 12 months |

Keep these assumptions below the main presentation; retain the three allocation policies, defaulting to proportional.

### Monthly update

1. Calculate household and facility requests.
2. Recharge the reservoir, limited by its capacity.
3. Allocate available water; record supply and shortfall without allowing negative stock.
4. Calculate grid demand after the solar contribution.
5. Restore eligible vegetation if reforestation is active.
6. Calculate indicators and well-being.
7. Advance one tick and record the plots.

Core equations:

- `IT energy = facility count × IT capacity × utilization × 720,000 kWh/month`
- `DC freshwater request = IT energy × WUE × (1 − reuse share) / 1,000,000`
- `Household water request = households × 500 × 30 / 1,000,000 ML/month`
- `Grid demand % = 100 × (DC average facility MW × (1 − solar share) + household MW) / grid capacity`

Grid demand above 100% indicates demand exceeding modeled capacity; it does **not** simulate blackouts or automatically shut facilities down.

### Land and recovery

- Protect river, roads, households, and indicator-sidebar patches from construction.
- Each facility occupies a unique 3 × 3 patch footprint and clears eligible vegetation within its surrounding 5 × 5 construction area.
- Save baseline vegetation after placing households but before building the initial facilities.
- Green Coverage equals current vegetation divided by that baseline.
- Reforestation gradually restores cleared, unoccupied land. It cannot regrow vegetation beneath buildings.
- Trees and wildlife visibility follow vegetation state. Wildlife is a habitat illustration, not a separately simulated population.
- A shrinking blue river display illustrates reservoir stock; its protected channel does not become buildable when water is scarce.

### Well-being score

Use:

`Score = 0.40 × Water Supply % + 0.30 × Grid Headroom % + 0.30 × Green Coverage %`

Where `Grid Headroom % = max(0, 100 − Grid Demand %)`.

Categories:

- Healthy: ≥75
- Moderate: ≥50 and <75
- Stressed: ≥25 and <50
- Critical: <25

Show the formula and explain that weights and thresholds are **illustrative teaching choices**, not measured human health. Do not force a Healthy starting label for high starting facility counts.

Exclude CO₂, pollution, waste heat, e-waste, disease, and independent population-health systems from V2. Solar changes grid reliance—not water directly; reforestation changes vegetation—not reservoir recharge.

## 5. Implementation order and acceptance tests

1. **UI prototype:** complete the landscape, controls, icons, indicators, legend, and graph layout using clearly identified preview values.
2. **Resource model:** connect manual construction, households, monthly water balance, and grid demand; remove all preview values.
3. **Recovery and scoring:** add the three interventions, vegetation recovery, and transparent condition categories.
4. **Verification and handoff:** update documentation, retain screenshots, and record approved decisions and verified results in the existing Obsidian project and daily notes.

Acceptance checks:

- Setup produces exactly the selected counts; facility 51 is rejected without clearing land.
- Changing starting sliders requires Setup; repeated Reset reproduces the same starting state.
- One default facility requests **0.504 ML/month**; ten households request **0.15 ML/month**; their combined grid demand is **2.85%**.
- Recycled water halves facility freshwater demand; solar leaves water demand unchanged.
- Reforestation is gradual and never restores occupied footprints.
- Water conservation holds at empty/full reservoirs and under every allocation policy.
- Grid readings above 100% remain visible rather than being numerically clamped.
- Well-being category boundaries at 25, 50, and 75 are tested.
- Viewing statistics or activating an already-enabled solution adds no ticks or duplicate benefits.
- Run baseline, expansion, individual-intervention, combined-intervention, and 50-facility/50-household scenarios.
- Inspect setup, shortage, recovery, and completion in the actual NetLogo application at both target sizes.
- Verify readable grayscale warnings, no overlapping buildings or labels, and no dependency on an internet connection.
- Preserve legacy tests and results; establish separate V2 experiments rather than asserting numerical equality between versions.

Planning only: no model changes or new-runtime validation have been performed in this turn.
