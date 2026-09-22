# AI Data Center Water-Energy Nexus NetLogo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a neutral, interactive NetLogo model that lets students explore how hypothetical AI data-center growth, cooling-water efficiency, water reuse, drought, electricity efficiency, renewable power, and water-allocation policy affect a shared freshwater supply, community water service, computing capacity, and operational carbon emissions.

**Architecture:** The model uses one-month ticks. Data-center turtles expand as modeled compute demand grows; a community turtle and the data centers request water from one shared water-source turtle. Transparent WUE- and PUE-based equations calculate water and energy use, an explicit allocation policy handles shortages, and BehaviorSpace compares reproducible scenarios. PAX Silica is context only: the model does not represent, predict, or evaluate the actual project.

**Tech Stack:** NetLogo 7.0.4, built-in NetLogo primitives only, BehaviorSpace, Markdown documentation, Git.

---

## 1. Modeling decision and academic framing

Use the presentation title **AI Data Center Water-Energy Nexus: A Scenario Model for the Philippines**. Keep **AI Data Center Simulation** as the assignment's short working title.

The correct framing is:

> This is a hypothetical teaching model inspired by Philippine discussions about AI-supporting infrastructure and resource use. It is not a forecast or digital twin of PAX Silica. Users choose assumptions and compare outcomes; the model does not label data-center development as inherently good or bad.

This distinction is necessary because the current official BCDA description calls PAX Silica a proposed manufacturing-driven industrial and innovation ecosystem, not a large data-center complex. BCDA said that only one or two of at least 30 interested companies were data-center operators. Its published water-supply and demand numbers refer to the entire proposed industrial development and must not be used as data-center calibration values.

### STS lens

- **Science:** measurable resource flows, explicit equations, units, assumptions, sensitivity analysis, and reproducible experiments.
- **Technology:** AI compute capacity, facility efficiency, cooling-water efficiency, water reuse, renewable electricity, and infrastructure expansion.
- **Society:** a shared water resource, community water service, allocation rules, policy choices, and competing infrastructure priorities.

### SDG mapping

| SDG | Model connection | Observable model outputs |
| --- | --- | --- |
| SDG 6 — Clean Water and Sanitation | Water-use efficiency, freshwater availability, drought, and access | reservoir level, freshwater withdrawal, WUE, shortage, community service level |
| SDG 9 — Industry, Innovation and Infrastructure | Growth and efficiency of AI-supporting infrastructure | number of facilities, served and unserved compute demand, PUE |
| SDG 12 — Responsible Consumption and Production | Resource efficiency and reuse | reclaimed-water share, total site water, freshwater withdrawal per unit of IT energy |
| SDG 13 — Climate Action | Operational energy and electricity-related emissions | facility electricity, renewable share, monthly and cumulative emissions |

Do not create a single “good/bad,” “sustainability,” or “environmental harm” score. Separate indicators make the tradeoffs visible without embedding a moral conclusion in an opaque formula.

## 2. Scope boundary

### Included in the MVP

- Data-center facility agents with IT capacity and monthly IT load.
- Compute demand that grows at a user-selected annual rate.
- New facilities added when demand exceeds installed capacity, up to a user-selected maximum.
- IT energy, total facility energy, site water, freshwater withdrawal, reclaimed water, and operational electricity emissions.
- One shared freshwater stock with recharge, drought reduction, and optional rainfall variability.
- One aggregate nearby community with a user-selected daily water requirement.
- Three explicit water-allocation rules: proportional, community-first, and data-center-first.
- Reproducible scenario presets and BehaviorSpace experiments.
- A visual world, monitors, plots, and a complete NetLogo Info tab.

### Excluded from the MVP

- A claim about PAX Silica's actual number, capacity, cooling system, water consumption, or emissions.
- Semiconductor manufacturing, mining, construction water, embodied carbon, jobs, prices, e-waste, biodiversity, and individual AI-query estimates.
- Detailed hydrology, groundwater flow, weather forecasting, power-grid dispatch, or facility construction delays.
- A policy recommendation or an automatic winner among scenarios.

These exclusions keep the first deliverable understandable and testable. Any one of them can become a separately scoped extension after the water-energy model is verified.

## 3. File map

- Create `models/ai-data-center-water-energy.nlogox` — executable NetLogo 7 model, interface, Info tab, shapes, and embedded BehaviorSpace experiments.
- Create `README.md` — setup, run, controls, scenario demonstration, testing, evidence categories, and project framing.
- Create `.gitignore` — ignore generated BehaviorSpace CSV files under `results/` while retaining the directory.
- Create `results/.gitkeep` — keeps the output directory in Git.

Keep the simulation in one `.nlogox` file. NetLogo 7 introduced this model format. The model is small enough that splitting procedures into include files would make classroom sharing and submission harder without adding a useful boundary. If the instructor explicitly requires NetLogo 6.4 or earlier, use its legacy `.nlogo` format and retest there before submission.

## 4. Model contract

### Time and units

- One tick is one 30-day month.
- Power and compute load are in megawatts (MW).
- Energy is in kilowatt-hours per month (kWh/month); plots may display gigawatt-hours (GWh/month).
- Water is in megaliters per month (ML/month), where 1 ML is 1,000,000 liters.
- WUE is liters of site water per kWh of IT energy.
- PUE is total facility energy divided by IT-equipment energy.
- Emissions are metric tonnes of CO2-equivalent per month.

### Causal sequence per tick

```text
AI demand growth
  -> required IT capacity
  -> data-center construction up to the maximum
  -> served and unserved IT load
  -> IT energy
     -> WUE -> total site water -> reclaimed-water credit -> freshwater request
     -> PUE -> total facility energy -> electricity mix -> operational emissions

drought + rainfall variability -> water-source recharge
community request + data-center request + allocation policy
  -> supplied water, shortages, service levels, and remaining reservoir stock
```

### Equations

For tick length `days-per-month = 30` and `hours-per-month = 720`:

```text
monthly growth factor = (1 + annual-growth-percent / 100)^(1 / 12)
compute demand this month = prior compute demand * monthly growth factor
served IT load MW = min(compute demand MW, installed IT capacity MW)
unserved compute MW = max(0, compute demand MW - installed IT capacity MW)

IT energy kWh = served IT load MW * 1000 * 720
facility energy kWh = IT energy kWh * PUE
site water ML = IT energy kWh * WUE L/kWh / 1,000,000
freshwater request ML = site water ML * (1 - reclaimed-water-percent / 100)

effective recharge ML = base recharge ML * (1 - drought-percent / 100) * rainfall factor
emissions tonnes = facility energy kWh * grid-factor kg/kWh
                   * (1 - renewable-percent / 100) / 1000
water stress ratio = total freshwater request ML / max(effective recharge ML, 0.000001)
```

WUE multiplies **IT energy**, not PUE-adjusted facility energy. Multiplying both WUE and PUE into the water equation would double-count facility overhead relative to the WUE definition.

### Interface variables

| Widget variable | Range / choices | Default | Meaning |
| --- | --- | --- | --- |
| `initial-data-centers` | 1–10, step 1 | 1 | facilities at setup |
| `facility-capacity-mw` | 10–200, step 10 | 50 | IT capacity of each facility |
| `initial-utilization-percent` | 10–100, step 5 | 70 | starting demand as a share of installed IT capacity |
| `annual-ai-demand-growth-percent` | 0–50, step 5 | 15 | compound annual growth in modeled compute demand |
| `maximum-data-centers` | 1–30, step 1 | 20 | construction ceiling |
| `pue` | 1.05–2.50, step 0.05 | 1.60 | total facility energy / IT energy |
| `wue-liters-per-kwh` | 0.0–3.0, step 0.1 | 1.0 | site water / IT energy; teaching default, not a PAX value |
| `reclaimed-water-percent` | 0–100, step 5 | 0 | share of site water not drawn from freshwater stock |
| `renewable-electricity-percent` | 0–100, step 5 | 25 | simplified zero-operational-emission electricity share |
| `grid-emissions-kg-per-kwh` | 0.0–1.0, step 0.05 | 0.50 | adjustable non-renewable electricity factor; illustrative default |
| `reservoir-capacity-ml` | 500–10,000, step 100 | 2,400 | maximum shared freshwater stock |
| `base-monthly-recharge-ml` | 0–1,500, step 50 | 650 | recharge without drought |
| `community-demand-ml-per-day` | 0–50, step 1 | 10 | aggregate nearby-community request |
| `drought-severity-percent` | 0–100, step 5 | 0 | proportional recharge reduction |
| `rainfall-variability-percent` | 0–50, step 5 | 0 | standard deviation of the monthly recharge multiplier |
| `simulation-years` | 1–20, step 1 | 10 | run duration |
| `seed-value` | 1–9,999, step 1 | 42 | reproducible random seed |
| `scenario` | Reference baseline; Rapid growth + drought; Efficiency + reuse | Reference baseline | values loaded by `load-scenario` |
| `water-allocation-policy` | proportional; community-first; data-center-first | proportional | shortage-sharing rule |

The defaults are a **teaching scenario**, not a forecast. `pue = 1.60` has a documented benchmark basis; every location-specific value remains adjustable and is labeled illustrative until the research team supplies an authoritative Philippine dataset.

### Scenario presets

All three scenarios use one initial 50 MW facility, 70% initial utilization, a 20-facility ceiling, a 2,400 ML reservoir, 650 ML/month base recharge, 10 ML/day community demand, 10 years, no rainfall randomness, and proportional allocation unless the presenter changes the allocation chooser.

| Parameter | Reference baseline | Rapid growth + drought | Efficiency + reuse |
| --- | ---: | ---: | ---: |
| Annual AI-demand growth | 15% | 30% | 30% |
| PUE | 1.60 | 1.60 | 1.30 |
| WUE | 1.0 L/kWh | 1.2 L/kWh | 0.4 L/kWh |
| Reclaimed water | 0% | 0% | 60% |
| Renewable electricity | 25% | 25% | 75% |
| Drought severity | 0% | 40% | 40% |

The rapid-growth and efficiency scenarios intentionally share the same demand and drought settings. That isolates the effect of efficiency, reuse, and electricity mix instead of quietly giving the “sustainable” scenario easier growth conditions.

## 5. Definition of done

- The `.nlogox` file opens without a compiler error in NetLogo 7.0.4.
- `setup`, one `go` step, a 120-tick run, and all three presets complete without runtime errors.
- Formula self-tests pass with exact known inputs.
- At every tick, reservoir stock stays between zero and capacity; supplied water never exceeds requests or available stock; service percentages stay between 0 and 100; cumulative emissions never decrease.
- Higher WUE never produces lower site-water use when all other variables are held constant.
- Higher PUE never produces lower facility energy or operational emissions when all other variables are held constant.
- Higher reclaimed-water share reduces freshwater withdrawal without changing total site water.
- Higher renewable share reduces modeled operational emissions without changing IT energy.
- Water-allocation policy changes sector service levels during shortage but does not create water.
- The Info tab explicitly states that this is not a PAX Silica forecast.
- The README distinguishes sourced definitions, contextual facts, and illustrative scenario values.
- BehaviorSpace exports reproducible results for the scenario comparison and uncertainty experiment.

---

### Task 1: Establish the repository and evidence boundary

**Files:**
- Create: `README.md`
- Create: `.gitignore`
- Create: `results/.gitkeep`

- [ ] **Step 1: Add the failing repository checks**

Run before creating the files:

```powershell
Test-Path .\README.md
Test-Path .\models\ai-data-center-water-energy.nlogox
Test-Path .\results\.gitkeep
```

Expected: all three return `False` in the current empty repository.

- [ ] **Step 2: Create the README with the exact framing**

The README must contain these sections and statements:

```markdown
# AI Data Center Water-Energy Nexus

A neutral STS classroom simulation built in NetLogo. It explores how hypothetical AI data-center growth and infrastructure choices interact with a shared freshwater supply, community water service, and operational electricity emissions.

## Accuracy boundary

This model is a hypothetical Philippine scenario, not a forecast or digital twin of PAX Silica. PAX Silica is context for an STS discussion about infrastructure choices. No model parameter should be presented as an actual PAX Silica facility value.

## Run

1. Open `models/ai-data-center-water-energy.nlogox` in NetLogo 7.0.4.
2. Choose a scenario and click `load-scenario`.
3. Click `setup`.
4. Click `go`, or use `go-once` to advance one month.
5. Compare water stock, sector service levels, compute demand, energy, and emissions.

## Suggested classroom comparison

Run `Rapid growth + drought`, then `Efficiency + reuse` with the same demand and drought assumptions. Change only `water-allocation-policy` during a shortage to discuss who receives scarce water and who decides.

## Evidence labels

- Sourced definition: WUE and PUE formulas.
- Sourced context: official statements about PAX Silica and its proposed water system.
- Illustrative assumption: every scenario default not explicitly tied to a cited source.

## SDGs

- SDG 6: water efficiency, availability, and community service.
- SDG 9: infrastructure capacity and efficiency.
- SDG 12: resource efficiency and reclaimed water.
- SDG 13: operational electricity emissions and renewable power.

## Test

Click `run-self-tests` in the Interface. Use BehaviorSpace experiments `smoke-test`, `scenario-comparison`, and `rainfall-uncertainty` for repeatable validation.
```

- [ ] **Step 3: Add generated-result exclusions**

Create `.gitignore` with:

```gitignore
results/*.csv
!results/.gitkeep
```

Create the empty tracked file `results/.gitkeep`.

- [ ] **Step 4: Verify the evidence boundary is visible**

Run:

```powershell
rg -n "hypothetical|not a forecast|Illustrative assumption|SDG 6|SDG 13" README.md
```

Expected: every phrase is found.

- [ ] **Step 5: Commit the documentation boundary**

```powershell
git add README.md .gitignore results/.gitkeep
git commit -m "docs: define neutral STS simulation scope"
```

### Task 2: Create the NetLogo model shell and interface

**Files:**
- Create: `models/ai-data-center-water-energy.nlogox`

- [ ] **Step 1: Verify the required runtime**

NetLogo was not found on `PATH` or directly under `C:\Program Files` during planning. Install NetLogo 7.0.4 from the official site or locate an existing installation, then run:

```powershell
$netLogoConsole = Get-ChildItem -Path 'C:\Program Files' -Recurse -Filter 'NetLogo_Console.exe' -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName
if (-not $netLogoConsole) { throw 'NetLogo_Console.exe was not found.' }
& $netLogoConsole --version
```

Expected: output identifies NetLogo 7.0.4.

- [ ] **Step 2: Create and save the model shell**

In NetLogo, choose **File → New**, set world dimensions to `min-pxcor = -16`, `max-pxcor = 16`, `min-pycor = -12`, `max-pycor = 12`, and save as `models/ai-data-center-water-energy.nlogox`.

- [ ] **Step 3: Add controls in the left interface column**

Add widgets with exactly the variable names, ranges, defaults, and chooser values from **Interface variables**. Add buttons:

| Button label | Command | Forever |
| --- | --- | --- |
| load-scenario | `load-scenario` | no |
| setup | `setup` | no |
| go | `go` | yes |
| go-once | `go` | no |
| run-self-tests | `run-self-tests` | no |

- [ ] **Step 4: Add output widgets**

Add monitors for:

```text
months-elapsed
count data-centers
total-it-load-mw
unserved-compute-demand-mw
[stored-water-ml] of one-of water-sources
100 * [stored-water-ml] of one-of water-sources / [capacity-ml] of one-of water-sources
data-center-freshwater-request-ml
community-service-percent
data-center-water-service-percent
monthly-emissions-tonnes
cumulative-emissions-tonnes
```

Add three plots:

- `Shared Water`: `reservoir ML`, `community request ML`, `data-center request ML`.
- `Service Levels`: `community %`, `data-center %` with y-axis fixed at 0–100.
- `Energy and Emissions`: `facility GWh/month`, `emissions kt/month`.

- [ ] **Step 5: Save and perform the intentional compile failure**

Click **Check**.

Expected: compilation fails because procedures and breeds referenced by the widgets do not exist yet. This proves the interface is connected to the intended procedure names.

- [ ] **Step 6: Commit the model shell**

```powershell
git add models/ai-data-center-water-energy.nlogox
git commit -m "feat: scaffold NetLogo model interface"
```

### Task 3: Implement and test pure resource equations

**Files:**
- Modify: `models/ai-data-center-water-energy.nlogox`

- [ ] **Step 1: Add self-tests first**

Add this code before adding the reporters it calls:

```netlogo
to assert-close [test-name actual expected tolerance]
  if abs (actual - expected) > tolerance [
    error (word test-name " expected " expected " but got " actual)
  ]
end

to run-self-tests
  assert-close "one MW monthly IT energy" (monthly-it-energy-kwh-for 1) 720000 0.0001
  assert-close "WUE water conversion" (monthly-site-water-ml-for 720000 1) 0.72 0.0001
  assert-close "PUE facility energy" (monthly-facility-energy-kwh-for 720000 1.6) 1152000 0.0001
  assert-close "reclaimed water" (monthly-freshwater-ml-for 36 50) 18 0.0001
  assert-close "operational emissions" (monthly-emissions-tonnes-for 57600000 0.5 20) 23040 0.0001

  let community-first allocate-requests 100 80 40 "community-first"
  assert-close "community-first community" (item 0 community-first) 80 0.0001
  assert-close "community-first data center" (item 1 community-first) 20 0.0001

  let data-center-first allocate-requests 100 80 40 "data-center-first"
  assert-close "data-center-first community" (item 0 data-center-first) 60 0.0001
  assert-close "data-center-first data center" (item 1 data-center-first) 40 0.0001

  let proportional allocate-requests 100 80 40 "proportional"
  assert-close "proportional community" (item 0 proportional) 66.6666667 0.0001
  assert-close "proportional data center" (item 1 proportional) 33.3333333 0.0001

  print "SELF-TESTS PASSED"
end
```

- [ ] **Step 2: Check that the tests fail**

Click **Check**.

Expected: failure identifies `MONTHLY-IT-ENERGY-KWH-FOR` or another undefined reporter.

- [ ] **Step 3: Add the minimal calculation reporters**

```netlogo
to-report monthly-it-energy-kwh-for [it-load-mw-value]
  report it-load-mw-value * 1000 * 720
end

to-report monthly-site-water-ml-for [it-energy-kwh-value wue-value]
  report it-energy-kwh-value * wue-value / 1000000
end

to-report monthly-facility-energy-kwh-for [it-energy-kwh-value pue-value]
  report it-energy-kwh-value * pue-value
end

to-report monthly-freshwater-ml-for [site-water-ml-value reuse-percent-value]
  report site-water-ml-value * (1 - reuse-percent-value / 100)
end

to-report monthly-emissions-tonnes-for [facility-energy-kwh-value grid-factor-value renewable-percent-value]
  report facility-energy-kwh-value * grid-factor-value * (1 - renewable-percent-value / 100) / 1000
end

to-report allocate-requests [available-water-ml community-request-ml-value data-center-request-ml-value policy]
  let total-request-ml community-request-ml-value + data-center-request-ml-value
  if total-request-ml = 0 [ report (list 0 0) ]
  if total-request-ml <= available-water-ml [
    report (list community-request-ml-value data-center-request-ml-value)
  ]
  if policy = "community-first" [
    let community-supply min (list community-request-ml-value available-water-ml)
    report (list community-supply (min (list data-center-request-ml-value (available-water-ml - community-supply))))
  ]
  if policy = "data-center-first" [
    let data-center-supply min (list data-center-request-ml-value available-water-ml)
    report (list (min (list community-request-ml-value (available-water-ml - data-center-supply))) data-center-supply)
  ]
  let supply-ratio available-water-ml / total-request-ml
  report (list (community-request-ml-value * supply-ratio) (data-center-request-ml-value * supply-ratio))
end
```

- [ ] **Step 4: Run the calculation tests**

Click **Check**, then run `run-self-tests` from the Command Center.

Expected output:

```text
SELF-TESTS PASSED
```

- [ ] **Step 5: Commit the tested equations**

```powershell
git add models/ai-data-center-water-energy.nlogox
git commit -m "test: add verified water and energy equations"
```

### Task 4: Add agents, state, setup, and the visual world

**Files:**
- Modify: `models/ai-data-center-water-energy.nlogox`

- [ ] **Step 1: Add the state declarations**

```netlogo
globals [
  months-elapsed
  compute-demand-mw
  total-it-load-mw
  unserved-compute-demand-mw
  total-it-energy-kwh
  total-facility-energy-kwh
  data-center-site-water-ml
  data-center-freshwater-request-ml
  data-center-freshwater-supplied-ml
  community-freshwater-request-ml
  community-freshwater-supplied-ml
  effective-recharge-ml
  spilled-water-ml
  water-shortage-ml
  water-stress-ratio
  community-service-percent
  data-center-water-service-percent
  monthly-emissions-tonnes
  cumulative-emissions-tonnes
]

breed [data-centers data-center]
breed [communities community]
breed [water-sources water-source]

data-centers-own [
  it-capacity-mw
  it-load-mw
  it-energy-kwh-month
  facility-energy-kwh-month
  site-water-ml-month
  freshwater-request-ml
  freshwater-supplied-ml
  water-service-percent
]

communities-own [freshwater-request-ml freshwater-supplied-ml service-percent]
water-sources-own [stored-water-ml capacity-ml recharge-ml-this-month]
```

Do not declare widget variables such as `pue` or `wue-liters-per-kwh` in `globals`; NetLogo interface widgets already create those globals.

- [ ] **Step 2: Add setup and layout procedures**

```netlogo
to setup
  clear-all
  random-seed seed-value
  paint-world
  create-water-sources 1 [
    setxy 0 10
    set shape "circle"
    set size 4
    set color blue
    set capacity-ml reservoir-capacity-ml
    set stored-water-ml reservoir-capacity-ml
    set recharge-ml-this-month 0
    set label "shared water"
  ]
  create-communities 1 [
    setxy -10 -2
    set shape "house"
    set size 4
    set color green
    set freshwater-request-ml 0
    set freshwater-supplied-ml 0
    set service-percent 100
    set label "community"
  ]
  create-data-centers (min (list initial-data-centers maximum-data-centers)) [
    initialize-data-center
  ]
  set compute-demand-mw count data-centers * facility-capacity-mw * initial-utilization-percent / 100
  set months-elapsed 0
  set cumulative-emissions-tonnes 0
  reset-monthly-totals
  reset-ticks
  update-visuals
end

to paint-world
  ask patches [
    ifelse pycor > 6
      [ set pcolor 95 ]
      [ ifelse pxcor < -4 [ set pcolor 57 ] [ set pcolor 9.5 ] ]
  ]
end

to initialize-data-center
  setxy (5 + random-float 9) (-8 + random-float 11)
  set shape "square"
  set size 2.5
  set color cyan
  set it-capacity-mw facility-capacity-mw
  set it-load-mw 0
  set it-energy-kwh-month 0
  set facility-energy-kwh-month 0
  set site-water-ml-month 0
  set freshwater-request-ml 0
  set freshwater-supplied-ml 0
  set water-service-percent 100
  set label (word "DC " who)
end

to reset-monthly-totals
  set total-it-load-mw 0
  set unserved-compute-demand-mw 0
  set total-it-energy-kwh 0
  set total-facility-energy-kwh 0
  set data-center-site-water-ml 0
  set data-center-freshwater-request-ml 0
  set data-center-freshwater-supplied-ml 0
  set community-freshwater-request-ml 0
  set community-freshwater-supplied-ml 0
  set effective-recharge-ml 0
  set spilled-water-ml 0
  set water-shortage-ml 0
  set water-stress-ratio 0
  set community-service-percent 100
  set data-center-water-service-percent 100
  set monthly-emissions-tonnes 0
end
```

- [ ] **Step 3: Check setup behavior**

Click **Check**, then click `setup`.

Expected:

- one blue water source at the top;
- one green community on the left;
- the selected number of cyan data centers on the right;
- reservoir monitor equals `reservoir-capacity-ml`;
- all flow and emissions monitors are zero.

- [ ] **Step 4: Commit agent initialization**

```powershell
git add models/ai-data-center-water-energy.nlogox
git commit -m "feat: add data center community and water agents"
```

### Task 5: Implement the monthly simulation loop

**Files:**
- Modify: `models/ai-data-center-water-energy.nlogox`

- [ ] **Step 1: Add invariant checks before the loop**

```netlogo
to check-invariants
  let source one-of water-sources
  if [stored-water-ml] of source < -0.000001 [ error "Reservoir stock became negative." ]
  if [stored-water-ml] of source > [capacity-ml] of source + 0.000001 [ error "Reservoir exceeded capacity." ]
  if community-freshwater-supplied-ml > community-freshwater-request-ml + 0.000001 [ error "Community supply exceeded request." ]
  if data-center-freshwater-supplied-ml > data-center-freshwater-request-ml + 0.000001 [ error "Data-center supply exceeded request." ]
  if community-service-percent < 0 or community-service-percent > 100.000001 [ error "Community service left the 0-100 range." ]
  if data-center-water-service-percent < 0 or data-center-water-service-percent > 100.000001 [ error "Data-center service left the 0-100 range." ]
end
```

- [ ] **Step 2: Add demand growth and facility expansion**

```netlogo
to grow-compute-demand
  set compute-demand-mw compute-demand-mw * ((1 + annual-ai-demand-growth-percent / 100) ^ (1 / 12))
end

to expand-data-center-capacity
  let required-centers min (list maximum-data-centers (ceiling (compute-demand-mw / facility-capacity-mw)))
  if required-centers > count data-centers [
    create-data-centers (required-centers - count data-centers) [ initialize-data-center ]
  ]
end

to assign-it-load
  let installed-capacity-mw sum [it-capacity-mw] of data-centers
  set total-it-load-mw min (list compute-demand-mw installed-capacity-mw)
  set unserved-compute-demand-mw max (list 0 (compute-demand-mw - installed-capacity-mw))
  let load-per-center-mw total-it-load-mw / count data-centers
  ask data-centers [ set it-load-mw min (list it-capacity-mw load-per-center-mw) ]
end
```

- [ ] **Step 3: Add energy, water, and recharge calculations**

```netlogo
to calculate-resource-requests
  ask data-centers [
    set it-energy-kwh-month monthly-it-energy-kwh-for it-load-mw
    set facility-energy-kwh-month monthly-facility-energy-kwh-for it-energy-kwh-month pue
    set site-water-ml-month monthly-site-water-ml-for it-energy-kwh-month wue-liters-per-kwh
    set freshwater-request-ml monthly-freshwater-ml-for site-water-ml-month reclaimed-water-percent
  ]
  set total-it-energy-kwh sum [it-energy-kwh-month] of data-centers
  set total-facility-energy-kwh sum [facility-energy-kwh-month] of data-centers
  set data-center-site-water-ml sum [site-water-ml-month] of data-centers
  set data-center-freshwater-request-ml sum [freshwater-request-ml] of data-centers
  set community-freshwater-request-ml community-demand-ml-per-day * 30
  ask communities [ set freshwater-request-ml community-freshwater-request-ml ]
  set monthly-emissions-tonnes monthly-emissions-tonnes-for total-facility-energy-kwh grid-emissions-kg-per-kwh renewable-electricity-percent
  set cumulative-emissions-tonnes cumulative-emissions-tonnes + monthly-emissions-tonnes
end

to recharge-water-source
  let rainfall-factor 1
  if rainfall-variability-percent > 0 [
    set rainfall-factor max (list 0 (random-normal 1 (rainfall-variability-percent / 100)))
  ]
  set effective-recharge-ml base-monthly-recharge-ml * (1 - drought-severity-percent / 100) * rainfall-factor
  ask one-of water-sources [
    set recharge-ml-this-month effective-recharge-ml
    set spilled-water-ml max (list 0 (stored-water-ml + recharge-ml-this-month - capacity-ml))
    set stored-water-ml min (list capacity-ml (stored-water-ml + recharge-ml-this-month))
  ]
end
```

- [ ] **Step 4: Add shortage allocation and indicators**

```netlogo
to allocate-shared-water
  let source one-of water-sources
  let allocation allocate-requests ([stored-water-ml] of source) community-freshwater-request-ml data-center-freshwater-request-ml water-allocation-policy
  set community-freshwater-supplied-ml item 0 allocation
  set data-center-freshwater-supplied-ml item 1 allocation

  ask source [
    set stored-water-ml max (list 0 (stored-water-ml - community-freshwater-supplied-ml - data-center-freshwater-supplied-ml))
  ]
  ask communities [
    set freshwater-supplied-ml community-freshwater-supplied-ml
    ifelse freshwater-request-ml = 0
      [ set service-percent 100 ]
      [ set service-percent 100 * freshwater-supplied-ml / freshwater-request-ml ]
  ]
  ask data-centers [
    ifelse data-center-freshwater-request-ml = 0
      [ set freshwater-supplied-ml 0 set water-service-percent 100 ]
      [
        set freshwater-supplied-ml data-center-freshwater-supplied-ml * freshwater-request-ml / data-center-freshwater-request-ml
        set water-service-percent 100 * freshwater-supplied-ml / freshwater-request-ml
      ]
  ]

  set community-service-percent [service-percent] of one-of communities
  ifelse data-center-freshwater-request-ml = 0
    [ set data-center-water-service-percent 100 ]
    [ set data-center-water-service-percent 100 * data-center-freshwater-supplied-ml / data-center-freshwater-request-ml ]
  set water-shortage-ml (community-freshwater-request-ml + data-center-freshwater-request-ml) - (community-freshwater-supplied-ml + data-center-freshwater-supplied-ml)
  set water-stress-ratio (community-freshwater-request-ml + data-center-freshwater-request-ml) / max (list 0.000001 effective-recharge-ml)
end
```

- [ ] **Step 5: Add visual updates and `go`**

```netlogo
to update-visuals
  if any? water-sources [
    ask one-of water-sources [
      set color scale-color blue stored-water-ml 0 capacity-ml
      set label (word precision stored-water-ml 0 " ML")
    ]
  ]
  ask communities [
    set color ifelse-value (service-percent >= 99.999) [green] [orange]
  ]
  ask data-centers [
    set color ifelse-value (water-service-percent >= 99.999) [cyan] [violet]
  ]
end

to go
  if ticks >= simulation-years * 12 [ stop ]
  reset-monthly-totals
  set months-elapsed ticks + 1
  grow-compute-demand
  expand-data-center-capacity
  assign-it-load
  calculate-resource-requests
  recharge-water-source
  allocate-shared-water
  update-visuals
  check-invariants
  tick
end
```

- [ ] **Step 6: Run integration checks**

Use `setup`, then run these commands in the Command Center:

```netlogo
repeat 120 [ go ]
show ticks
show [stored-water-ml] of one-of water-sources
show community-service-percent
show cumulative-emissions-tonnes
```

Expected: `ticks` is 120; no invariant error occurs; reservoir stock is within its bounds; service level is within 0–100; cumulative emissions are non-negative.

- [ ] **Step 7: Commit the monthly dynamics**

```powershell
git add models/ai-data-center-water-energy.nlogox
git commit -m "feat: simulate monthly water energy and allocation flows"
```

### Task 6: Add neutral scenario presets

**Files:**
- Modify: `models/ai-data-center-water-energy.nlogox`

- [ ] **Step 1: Add the preset procedure**

```netlogo
to load-scenario
  set initial-data-centers 1
  set facility-capacity-mw 50
  set initial-utilization-percent 70
  set maximum-data-centers 20
  set grid-emissions-kg-per-kwh 0.50
  set reservoir-capacity-ml 2400
  set base-monthly-recharge-ml 650
  set community-demand-ml-per-day 10
  set rainfall-variability-percent 0
  set simulation-years 10
  set seed-value 42
  set water-allocation-policy "proportional"

  if scenario = "Reference baseline" [
    set annual-ai-demand-growth-percent 15
    set pue 1.60
    set wue-liters-per-kwh 1.0
    set reclaimed-water-percent 0
    set renewable-electricity-percent 25
    set drought-severity-percent 0
    stop
  ]
  if scenario = "Rapid growth + drought" [
    set annual-ai-demand-growth-percent 30
    set pue 1.60
    set wue-liters-per-kwh 1.2
    set reclaimed-water-percent 0
    set renewable-electricity-percent 25
    set drought-severity-percent 40
    stop
  ]
  if scenario = "Efficiency + reuse" [
    set annual-ai-demand-growth-percent 30
    set pue 1.30
    set wue-liters-per-kwh 0.4
    set reclaimed-water-percent 60
    set renewable-electricity-percent 75
    set drought-severity-percent 40
    stop
  ]
  error (word "Unknown scenario: " scenario)
end
```

- [ ] **Step 2: Test isolation between scenarios**

Select each scenario, click `load-scenario`, and confirm its six scenario-specific controls match the table. In particular, verify that both `Rapid growth + drought` and `Efficiency + reuse` retain 30% growth and 40% drought.

- [ ] **Step 3: Test allocation-policy neutrality**

Load `Rapid growth + drought`, run until a shortage occurs, record total supplied water, then rerun the same seed and parameters under all three allocation rules.

Expected:

- total supplied water is the same for all three rules;
- community and data-center service levels differ;
- proportional allocation gives both sectors the same percentage service level;
- no policy creates water or eliminates the physical shortage.

- [ ] **Step 4: Commit scenario controls**

```powershell
git add models/ai-data-center-water-energy.nlogox
git commit -m "feat: add reproducible growth and sustainability scenarios"
```

### Task 7: Complete plots and the Info tab

**Files:**
- Modify: `models/ai-data-center-water-energy.nlogox`
- Modify: `README.md`

- [ ] **Step 1: Add plot update commands**

For `Shared Water`, use:

```netlogo
set-current-plot "Shared Water"
set-current-plot-pen "reservoir ML"
plot [stored-water-ml] of one-of water-sources
set-current-plot-pen "community request ML"
plot community-freshwater-request-ml
set-current-plot-pen "data-center request ML"
plot data-center-freshwater-request-ml
```

For `Service Levels`, use:

```netlogo
set-current-plot "Service Levels"
set-current-plot-pen "community %"
plot community-service-percent
set-current-plot-pen "data-center %"
plot data-center-water-service-percent
```

For `Energy and Emissions`, use:

```netlogo
set-current-plot "Energy and Emissions"
set-current-plot-pen "facility GWh/month"
plot total-facility-energy-kwh / 1000000
set-current-plot-pen "emissions kt/month"
plot monthly-emissions-tonnes / 1000
```

Set each plot's update commands to its corresponding block rather than calling a separate plotting procedure from `go`.

- [ ] **Step 2: Write the Info tab**

Use NetLogo's standard headings:

```text
WHAT IS IT?
This neutral STS classroom model explores how hypothetical AI data-center growth can interact with a shared freshwater supply, nearby community demand, infrastructure efficiency, and operational electricity emissions. It is not a forecast or digital twin of PAX Silica.

HOW IT WORKS
One tick represents a 30-day month. Compute demand grows, facilities are added when installed capacity is insufficient, and served IT load determines IT energy. WUE converts IT energy into site water. Reclaimed water reduces freshwater withdrawal but does not erase total site water use. PUE converts IT energy into total facility energy. The chosen grid factor and renewable share determine modeled operational emissions. The community and data centers request water from one recharging source; the selected allocation policy determines who receives scarce water.

HOW TO USE IT
Choose a scenario, click LOAD-SCENARIO, click SETUP, and click GO. Compare the rapid-growth-and-drought scenario with the efficiency-and-reuse scenario. During a shortage, change only the allocation policy and discuss who benefits, who bears the shortfall, and who should decide.

THINGS TO NOTICE
More facilities do not automatically mean a shortage; outcomes also depend on demand, WUE, reuse, recharge, drought, and community demand. Better PUE lowers electricity and modeled emissions but does not directly change the WUE water equation. Reclaimed water lowers freshwater withdrawal while total site water can remain unchanged. Allocation policy redistributes shortage without creating more water.

THINGS TO TRY
Hold growth and drought constant while changing WUE. Hold WUE constant while changing reclaimed water. Hold energy constant while changing renewable share. Create a shortage and compare all three allocation rules. Turn on rainfall variability, keep the seed fixed, and then change only the seed.

MODEL ASSUMPTIONS AND LIMITATIONS
All scenario values except explicitly cited metric benchmarks are illustrative. The shared reservoir is an aggregate stock, not a hydrological model. Renewable electricity is treated as zero operational emissions, and lifecycle emissions are excluded. A data-center water shortfall is displayed but does not shut down IT load in the same tick. Construction delay, manufacturing, jobs, costs, biodiversity, e-waste, groundwater flow, and embodied impacts are outside the MVP.

STS AND SDGS
Science appears in transparent equations and experiments. Technology appears in compute, cooling-water efficiency, facility efficiency, reuse, and electricity mix. Society appears in shared-resource access and allocation policy. The model connects to SDG 6 through water efficiency and access, SDG 9 through infrastructure, SDG 12 through resource efficiency and reuse, and SDG 13 through energy-related emissions.

SOURCES
Presidential Communications Office / BCDA, “BCDA says Pax Silica project could generate over 130,000 high-quality jobs,” July 23, 2026.
U.S. Department of Energy FEMP, “Cooling Water Efficiency Opportunities for Federal Data Centers,” January 9, 2019.
U.S. Department of Energy FEMP, “Best Practices Guide for Energy-Efficient Data Center Design,” July 2024.
NetLogo User Manual, “BehaviorSpace.”
IBM, “What Is an AI Data Center?”
Penn State Institute of Energy and the Environment, “Why AI Uses So Much Energy and What We Can Do About It.”
Brookings Institution, “The Future of Data Centers.”

CREDITS
Created for a Science, Technology, and Society course project. Model authors must add their group names before submission.
```

- [ ] **Step 3: Add source URLs to the README**

Use the exact links in **Source register** below and classify each as official metric definition, official project context, course-draft background, or software documentation.

- [ ] **Step 4: Verify plots and documentation**

Run each preset for 120 ticks. Confirm all pens draw, axes are readable, and no pen silently mixes incompatible units. Search the Info tab and README for `not a forecast` and `illustrative`.

- [ ] **Step 5: Commit the explanatory interface**

```powershell
git add models/ai-data-center-water-energy.nlogox README.md
git commit -m "docs: explain STS model assumptions controls and evidence"
```

### Task 8: Add BehaviorSpace experiments and headless verification

**Files:**
- Modify: `models/ai-data-center-water-energy.nlogox`

- [ ] **Step 1: Create `smoke-test` in BehaviorSpace**

Use:

```text
Setup commands: load-scenario setup
Go commands: go
Stop condition: ticks >= 120
Repetitions: 1
Run metrics every step: false
scenario: "Reference baseline"
Metrics:
  count data-centers
  compute-demand-mw
  unserved-compute-demand-mw
  [stored-water-ml] of one-of water-sources
  community-service-percent
  data-center-water-service-percent
  cumulative-emissions-tonnes
  water-stress-ratio
```

- [ ] **Step 2: Run the smoke test headlessly**

```powershell
$netLogoConsole = Get-ChildItem -Path 'C:\Program Files' -Recurse -Filter 'NetLogo_Console.exe' -ErrorAction Stop | Select-Object -First 1 -ExpandProperty FullName
& $netLogoConsole --headless --model "$PWD\models\ai-data-center-water-energy.nlogox" --experiment 'smoke-test' --table "$PWD\results\smoke-test.csv"
```

Expected: exit code 0 and `results/smoke-test.csv` contains one completed run with non-negative metrics.

- [ ] **Step 3: Create `scenario-comparison`**

Use the same setup, go, stop condition, and metrics as `smoke-test`. Set repetitions to 1 and enumerate `scenario` with exactly:

```text
"Reference baseline"
"Rapid growth + drought"
"Efficiency + reuse"
```

Expected: three runs. Rapid growth + drought should show greater freshwater pressure than the reference under the supplied teaching values. Efficiency + reuse should use less freshwater and produce fewer operational emissions than Rapid growth + drought because demand and drought are held equal while resource-efficiency settings differ.

- [ ] **Step 4: Create `rainfall-uncertainty`**

Use `scenario = "Rapid growth + drought"`, set `rainfall-variability-percent = 20`, enumerate `seed-value` from 1 through 30, and record the same final metrics.

Expected: 30 runs with variation in final reservoir and service levels. Repeating the experiment produces identical results for each seed.

- [ ] **Step 5: Run all experiments**

```powershell
& $netLogoConsole --headless --model "$PWD\models\ai-data-center-water-energy.nlogox" --experiment 'scenario-comparison' --table "$PWD\results\scenario-comparison.csv"
& $netLogoConsole --headless --model "$PWD\models\ai-data-center-water-energy.nlogox" --experiment 'rainfall-uncertainty' --table "$PWD\results\rainfall-uncertainty.csv"
```

Expected: both commands exit 0; output files are ignored by Git; scenario comparison has 3 completed rows and uncertainty has 30 completed rows, excluding metadata/header lines.

- [ ] **Step 6: Commit the experiments**

```powershell
git add models/ai-data-center-water-energy.nlogox
git commit -m "test: add BehaviorSpace scenario validation"
```

### Task 9: Run the presentation acceptance test

**Files:**
- Modify: `README.md`
- Modify: `models/ai-data-center-water-energy.nlogox`

- [ ] **Step 1: Perform a five-minute demonstration**

Use this sequence:

1. Explain the shared water source and the three agent types.
2. Run `Reference baseline` for 120 months.
3. Run `Rapid growth + drought` and point to demand, reservoir, service levels, and emissions.
4. Run `Efficiency + reuse` with identical growth and drought.
5. Re-run a shortage while changing only the allocation policy.
6. End with the limitation: the model shows conditional outcomes from chosen assumptions, not the actual future of PAX Silica.

- [ ] **Step 2: Conduct the neutrality check**

Search visible interface text, Info tab, and README for unsupported claims that the project “will,” “must,” “definitely,” “solves,” “destroys,” or “proves” an outcome. Replace causal certainty about the real project with conditional model language such as “under these assumptions,” “the model shows,” and “could.”

- [ ] **Step 3: Conduct the parameter provenance check**

For every default in the interface, confirm that README or Info identifies it as either sourced or illustrative. Confirm that the PAX-wide 65–90 ML/day demand and 120 ML/day proposed surface-water supply are mentioned only as whole-development context, if included at all, and never assigned to the data-center agents.

- [ ] **Step 4: Conduct the clean-repository check**

```powershell
git status --short
git diff --check
git log --oneline --decorate -9
```

Expected: only intentional uncommitted presentation adjustments, if any; no whitespace errors; task commits are visible.

- [ ] **Step 5: Commit the finished package**

```powershell
git add README.md models/ai-data-center-water-energy.nlogox
git commit -m "chore: finalize STS NetLogo simulation package"
```

## 6. Source register

### Official project context

- [PCO / BCDA: Pax Silica manufacturing-and-innovation framing and proposed water system](https://pco.gov.ph/news_releases/bcda-says-pax-silica-project-could-generate-over-130000-high-quality-jobs/)

### Metric definitions and benchmarks

- [U.S. DOE: Cooling Water Efficiency Opportunities for Federal Data Centers](https://www.energy.gov/cmei/femp/cooling-water-efficiency-opportunities-federal-data-centers)
- [U.S. DOE: Best Practices Guide for Energy-Efficient Data Center Design](https://www.energy.gov/sites/default/files/2024-07/best-practice-guide-data-center-design.pdf)

### NetLogo implementation and uniqueness check

- [NetLogo BehaviorSpace manual](https://docs.netlogo.org/behaviorspace.html)
- [Official NetLogo Models Library repository](https://github.com/NetLogo/models)

The current official Models Library file tree was checked for model filenames matching `data center`, `water usage`, `artificial intelligence`, `AI data`, `cooling`, and `water stress`; no obvious matching model title was found. This supports originality, but the final group should still mention that it performed a library search rather than claiming no related model exists anywhere.

### Sources already present in the group's Google Doc

- [IBM: What Is an AI Data Center?](https://www.ibm.com/think/topics/ai-data-center)
- [Penn State Institute of Energy and the Environment: Why AI Uses So Much Energy](https://iee.psu.edu/news/blog/why-ai-uses-so-much-energy-and-what-we-can-do-about-it)
- [Brookings Institution: The Future of Data Centers](https://www.brookings.edu/articles/the-future-of-data-centers/)
- [Group project document](https://docs.google.com/document/d/1kEvWeX4g3D4XUFAYUSCoRyC-APubSqgBAyP7mwPBvUk/edit)

## 7. Self-review results

- **Spec coverage:** The plan covers the STS requirement, model-library originality check, SDGs 6/9/12/13, neutral framing, adjustable variables, data-center growth, water resources, environment-related emissions, sustainable alternatives, tests, documentation, and presentation flow.
- **Scope discipline:** PAX-specific prediction, manufacturing, jobs, e-waste, biodiversity, and detailed hydrology are excluded from the MVP so the group can finish and explain one coherent simulation.
- **Type and name consistency:** Widget, global, breed, owned-variable, reporter, and procedure names are consistent across equations, code, tests, monitors, and BehaviorSpace specifications.
- **Evidence discipline:** WUE and PUE are sourced; the model values are labeled illustrative; the PAX-wide water numbers are not used as data-center inputs.
- **Placeholder scan:** The plan contains no unfinished implementation placeholder. Group member names are intentionally left out of generated documentation because authorship should be entered by the group in the final artifact.
