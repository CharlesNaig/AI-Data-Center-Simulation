# AI Data Center Water-Energy Nexus

**V2.1.3** is a NetLogo classroom simulation for Science, Technology, and Society (STS). It explores how data centers and households share water, draw electricity, and change land cover in a hypothetical Philippine community. The main focus is **SDG 6**, particularly water access and efficiency. PAX Silica provides project context. The model uses illustrative assumptions and does not forecast the real development.

## Open and run

1. Open [the model](models/ai-data-center-community-v2.nlogox) in **NetLogo 7.0.4**. Keep the [assets folder](assets/) beside the models folder so NetLogo can load the map artwork.
2. Choose **starting-data-centers** (0–50) and **starting-households** (1–50). The saved interface starts with **4 data centers and 16 households**. Click **Reset World** to apply your choices.
3. Use **Next Month** to advance one 30-day month or **Play / Pause** to run continuously. The default run freezes after 120 months. **Reset World** starts a new run, clears chart history, and switches off the eco actions.
4. Use **Data Center +1 / −1** and **Household +1 / −1** to change counts during a run. Each minus button removes the most recently placed building of that type. Demand and map indicators update immediately. Water allocation and chart history update on the next monthly step.
5. Use **View Statistics** to see current requests and the last completed month's water balance. Open it after advancing at least one month to see observed allocations.

The **Advanced assumptions** panel is to the right of the map. Its controls are optional. Drag a slider or type in its number box, then click **Reset World** to apply the new inputs. On smaller windows, scroll horizontally for the panel and vertically for the charts. Use **Tabs → Command Center** to free up space.

## Eco actions

| Control | Effect |
| --- | --- |
| **Solar Power On / Off** | Supplies 40% of facility electricity and reduces grid demand. |
| **Water Recycling On / Off** | Replaces 50% of facility freshwater demand with recycled water. |
| **Reforestation On / Off** | Restores eligible cleared ground gradually over up to 12 active months. Switching it off pauses further regrowth. |

The map shows which actions are active. A facility occupies a 3 × 3 footprint and clears vegetation in its surrounding 5 × 5 area. Removing a facility frees its footprint. Eligible cleared patches recover while reforestation is on. Roads and the river stay protected.

## Read the results

| Indicator | What it measures |
| --- | --- |
| **Water Supply** | Reservoir stock as a share of capacity. Use **View Statistics** for unmet monthly demand and how much each group received. |
| **Grid Demand** | Requested grid load as a share of grid capacity. The chart includes a 100% reference and can show demand above it. |
| **Green Coverage** | Vegetation compared with the baseline recorded after the starting homes are placed and before the starting data centers are built. |
| **Community Well-Being** | An illustrative score weighted 40% reservoir fullness, 30% grid headroom, and 30% green coverage. |

The three charts below the map show water supply, grid demand, and green coverage over elapsed years. They record the starting state and one point per month. **ML** means million liters.

## Model assumptions

| Setting | Saved default |
| --- | --- |
| IT capacity per data center and utilization | 1 MW and 70% |
| Power usage effectiveness (PUE) | 1.6 |
| Water usage effectiveness (WUE) | 1 L per IT kWh |
| Water and power use per household | 500 L/day and 2 kW |
| Reservoir capacity and monthly recharge | 40 ML and 8 ML/month |
| Grid capacity | 40 MW |
| Water allocation | Proportional |
| Run duration | 120 months |

IT energy depends on the number of facilities, capacity, utilization, and hours per month. PUE converts IT load to facility electricity demand. WUE determines site water demand from IT energy, and recycling reduces the freshwater portion. Each month the reservoir recharges, spills any excess, and shares available water under the selected policy. **Proportional** shares shortages according to demand. **Community-first** prioritizes households, while **data-center-first** prioritizes facilities.

## Compare scenarios in BehaviorSpace

In NetLogo, open **Tools → BehaviorSpace** and run **v2-scenarios**. This experiment uses its own starting settings and runs seven 72-month cases. These cover baseline, expansion, each eco action on its own, all three together, and a case with 50 data centers and 50 households. The baseline starts with one data center and ten households. Expansion adds 19 data centers after month 12. Eco actions start after month 24 in the cases that use them.

In NetLogo 7.0.4, the baseline ended month 72 with 39.35 ML in the reservoir and no monthly shortfall. The expansion case ended with an empty reservoir and 2.23 ML of unmet demand that month. Comparing the solar and recycled-water cases shows how the two actions address different pressures under the same starting conditions.

## Credits and submission

- The model bundles artwork for offline use. See the credits for the [Icons8](https://icons8.com) assets. [Artwork credits](assets/CREDITS.md)
- PAX Silica project background. [PCO / BCDA project context](https://pco.gov.ph/news_releases/bcda-says-pax-silica-project-could-generate-over-130000-high-quality-jobs/)
- Energy and water metric background. [U.S. DOE FEMP cooling-water efficiency guidance](https://www.energy.gov/cmei/femp/cooling-water-efficiency-opportunities-federal-data-centers)
- Course collaboration. [Project Ideas working document](https://docs.google.com/document/d/1kEvWeX4g3D4UFAYUSCoRyC-APubSqgBAyP7mwPBvUk/edit)

Before submission, add group members to the model's **Info** tab and confirm that the instructor can open NetLogo 7's .nlogox format. Present the results as outcomes under the selected teaching assumptions.
