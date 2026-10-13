import org.nlogo.headless.HeadlessWorkspace;

class CheckModel {
  static HeadlessWorkspace w;
  static int checks;
  static void command(String code) throws Exception { w.command(code); }
  static Object report(String code) throws Exception { return w.report(code); }
  static void check(String label, String expression) throws Exception {
    if (!Boolean.TRUE.equals(report(expression))) throw new AssertionError(label + ": " + expression);
    checks++;
  }
  static void close(String label, String expression, double expected) throws Exception {
    double actual = ((Number)report(expression)).doubleValue();
    if (Math.abs(actual - expected) > 1e-8) throw new AssertionError(label + ": " + actual + " != " + expected);
    checks++;
  }
  static Object snapshot() throws Exception {
    return report("(list ticks count data-centers count households reservoir-ml dc-request-ml household-request-ml " +
      "grid-demand-percent green-coverage-percent wellbeing-score recycled-water? solar? reforestation? " +
      "last-household-request last-dc-request household-supplied-ml dc-supplied-ml shortfall-ml spilled-ml " +
      "map [p -> [(list vegetation occupied? land-kind)] of p] sort patches)");
  }
  static void unchanged(String label, Object expected) throws Exception {
    if (!snapshot().equals(expected)) throw new AssertionError(label);
    checks++;
  }
  static void defaults() throws Exception { command("teaching-defaults setup"); }
  public static void main(String[] args) throws Exception {
    w = HeadlessWorkspace.newInstance();
    try {
      w.open(args[0]);
      defaults();
      check("default counts and no decorative agents", "count households = 10 and count data-centers = 1 and count turtles = 11");
      close("default DC water", "dc-request-ml", 0.504);
      close("default households water", "household-request-ml", 0.15);
      close("default grid", "grid-demand-percent", 2.85);
      close("initial stock", "reservoir-ml", 40);
      Object initial = snapshot();
      Object layout = report("(list house-slots tree-sites)");
      check("compact facility compound order", "item 0 facility-slots = [69 31] and item 1 facility-slots = [74 31] and item 5 facility-slots = [79 36]");
      check("larger normal-view art", "bitmap:width server-icon = 48 and bitmap:width solar-icon = 44 and bitmap:width (item 2 house-icons) = 32");
      command("setup"); unchanged("deterministic reset", initial);
      if (!layout.equals(report("(list house-slots tree-sites)"))) throw new AssertionError("Natural layout changed on Reset");
      checks++;
      check("scattered homes have unique physical footprints", "length remove-duplicates map [xy -> list round item 0 xy round item 1 xy] house-slots = 50");
      check("tree positions are irregular", "length tree-sites > 35 and length remove-duplicates map [site -> item 1 site] tree-sites = length tree-sites");
      check("saplings threshold", "vegetation-art-stage 0.099 = \"none\" and vegetation-art-stage 0.10 = \"sapling\" and vegetation-art-stage 0.749 = \"sapling\" and vegetation-art-stage 0.75 = \"mature\"");
      command("set starting-data-centers 50 set starting-households 50 set pue 3 set duration-months 120");
      unchanged("pending controls cannot mutate model", initial);
      command("go"); close("one month", "ticks", 1);
      close("default withdrawal", "reservoir-ml", 39.346);
      close("full reservoir spills recharge", "spilled-ml", 8);
      close("cached grid despite pue edit", "grid-demand-percent", 2.85);
      close("cached duration", "run-months", 72);
      command("setup");
      check("reset applies controls", "count households = 50 and count data-centers = 50 and run-months = 120 and power-overhead = 3");
      for (int homes : new int[]{1, 10, 50}) {
        for (int centers : new int[]{1, 50}) {
          command("teaching-defaults set starting-households " + homes + " set starting-data-centers " + centers + " setup");
          check("selected counts", "count households = " + homes + " and count data-centers = " + centers + " and count turtles = " + (homes + centers));
          check("adaptive facility art", "bitmap:width server-icon = " + (centers <= 10 ? 48 : centers <= 20 ? 40 : 32));
          check("house slot indexes", "sort [slot-index] of households = n-values count households [i -> i]");
          check("facility slot indexes", "sort [slot-index] of data-centers = n-values count data-centers [i -> i]");
          check("independent facility positions", "all? data-centers [list xcor ycor = item slot-index facility-slots]");
          check("independent house positions", "all? households [list xcor ycor = item slot-index house-slots]");
          check("unique facilities", "length remove-duplicates [list xcor ycor] of data-centers = count data-centers");
          check("nine patches per facility", "count patches with [occupied? and land-kind = \"land\"] = 9 * count data-centers");
          check("clearing areas protected", "all? data-centers [not any? patches with [abs (pxcor - [xcor] of myself) <= 2 and abs (pycor - [ycor] of myself) <= 2 and land-kind != \"land\"]]");
          check("protected land", "not any? patches with [member? land-kind [\"river\" \"road\" \"sidebar\" \"frame\"] and (occupied? or baseline-green != 0 or vegetation != 0)]");
          check("baseline excludes homes", "not any? patches with [land-kind = \"home\" and baseline-green != 0]");
          check("nine protected patches per home", "count patches with [land-kind = \"home\"] = 9 * count households");
          check("river preserved at empty stock", "all? patches with [pxcor >= 1 and pxcor <= 93 and pycor >= 5 and pycor <= 51 and abs (pxcor - river-center pycor) <= 3] [land-kind = \"river\"]");
        }
      }
      close("50 DC 50 households grid above 100", "grid-demand-percent", 140.25);
      check("high count not forced Healthy", "condition-for wellbeing-score != \"Healthy\"");
      Object maximum = snapshot();
      command("build-more"); unchanged("facility 51 rejected without land or resource changes", maximum);
      command("set starting-data-centers 51");
      boolean rejected = false;
      try { command("setup"); } catch (Exception expected) { rejected = true; }
      if (!rejected) throw new AssertionError("Invalid starting count accepted");
      unchanged("invalid setup preserves prior run", maximum);
      defaults(); command("go");
      double stock = ((Number)report("reservoir-ml")).doubleValue();
      command("build-more");
      close("build without tick", "ticks", 1); close("build without withdrawal", "reservoir-ml", stock);
      close("build updates demand", "dc-request-ml", 1.008);
      close("build preserves last-month request", "last-dc-request", 0.504);

      defaults();
      command("enable-solution \"Recycled water (50%)\"");
      close("reuse halves freshwater", "dc-request-ml", 0.252);
      close("reuse retains site water", "site-water-ml", 0.504);
      close("reuse no grid effect", "grid-demand-percent", 2.85);
      close("reuse no household effect", "household-request-ml", 0.15);
      close("reuse no stock withdrawal", "reservoir-ml", 40);
      Object reuse = snapshot(); command("enable-solution \"Recycled water (50%)\""); unchanged("reuse idempotent", reuse);
      defaults(); command("enable-solution \"Solar energy (40%)\"");
      close("solar grid", "grid-demand-percent", 1.73);
      close("solar no water effect", "dc-request-ml", 0.504);
      Object solar = snapshot(); command("enable-solution \"Solar energy (40%)\""); unchanged("solar idempotent", solar);
      defaults(); command("go"); double unchangedWater = ((Number)report("reservoir-ml")).doubleValue();
      defaults(); command("enable-solution \"Solar energy (40%)\" go"); close("solar monthly water isolation", "reservoir-ml", unchangedWater);

      command("teaching-defaults set starting-data-centers 20 setup");
      double green = ((Number)report("green-coverage-percent")).doubleValue();
      command("enable-solution \"Reforestation (12 months)\"");
      close("no instant regrowth", "green-coverage-percent", green);
      Object forest = snapshot(); command("enable-solution \"Reforestation (12 months)\""); unchanged("reforestation idempotent", forest);
      command("go");
      close("one month regrowth", "min [vegetation] of patches with [baseline-green = 1 and not occupied?]", 1.0 / 12);
      close("reforestation monthly water isolation", "reservoir-ml", 40 - 10.08 - 0.15);
      close("reforestation grid isolation", "grid-demand-percent", 56.05);
      command("repeat 10 [go]");
      check("not fully restored at month 11", "any? patches with [baseline-green = 1 and not occupied? and vegetation < 1]");
      command("go");
      check("full eligible restoration at month 12", "all? patches with [baseline-green = 1 and not occupied?] [vegetation = 1]");
      check("never restore occupied footprints", "all? patches with [occupied?] [vegetation = 0]");
      command("build-more");
      check("new build clears restored ground", "any? patches with [baseline-green = 1 and not occupied? and vegetation = 0]");
      command("repeat 12 [go]");
      check("later construction gets own 12 months", "all? patches with [baseline-green = 1 and not occupied?] [vegetation = 1]");

      for (String policy : new String[]{"proportional", "community-first", "data-center-first"}) {
        for (double available : new double[]{0, 10, 100, 200}) {
          String allocation = "allocate-requests " + available + " 80 40 \"" + policy + "\"";
          close("allocation conservation " + policy, "sum (" + allocation + ")", Math.min(available, 120));
        }
        close("zero request " + policy, "sum allocate-requests 0 0 0 \"" + policy + "\"", 0);
        for (int initialStock : new int[]{0, 40}) {
          for (int recharge : new int[]{0, 8, 80}) {
            command("teaching-defaults set starting-data-centers 50 set starting-households 50 set recharge-ml-month " + recharge +
              " set water-allocation-policy \"" + policy + "\" setup set reservoir-ml " + initialStock + " go");
            close("water conservation " + policy, "balance-error-ml", 0);
            close("expected reservoir " + policy, "reservoir-ml", Math.max(0, Math.min(40, initialStock + recharge) - 25.95));
            command("check-invariants");
          }
        }
        command("teaching-defaults set wue-l-per-kwh 0 set household-liters-day 0 set water-allocation-policy \"" + policy + "\" setup go");
        check("zero requests safe " + policy, "shortfall-ml = 0 and reservoir-ml = 40 and service-percent household-supplied-ml last-household-request = 100");
      }
      close("community priority", "item 0 allocate-requests 100 80 40 \"community-first\"", 80);
      close("DC priority", "item 0 allocate-requests 100 80 40 \"data-center-first\"", 60);
      close("proportional priority", "item 0 allocate-requests 100 80 40 \"proportional\"", 100.0 * 80 / 120);
      double[] edges = {0, 24.999, 25, 49.999, 50, 74.999, 75, 100};
      String[] labels = {"Critical", "Critical", "Stressed", "Stressed", "Moderate", "Moderate", "Healthy", "Healthy"};
      for (int i = 0; i < edges.length; i++) check("category " + edges[i], "condition-for " + edges[i] + " = \"" + labels[i] + "\"");
      defaults();
      check("score formula", "abs (wellbeing-score - (0.4 * water-supply-percent + 0.3 * max (list 0 (100 - grid-demand-percent)) + 0.3 * green-coverage-percent)) < 0.00000001");
      Object beforeStats = snapshot(); report("statistics-text"); unchanged("statistics reporter is read-only", beforeStats);
      command("repeat 72 [go]"); close("duration", "ticks", 72);
      command("export-all-plots \"../results/baseline-plots.csv\"");
      close("no auto growth", "count data-centers", 1);
      Object complete = snapshot();
      command("go build-more enable-solution \"Solar energy (40%)\" enable-solution \"Recycled water (50%)\" enable-solution \"Reforestation (12 months)\"");
      unchanged("completion freezes all mutations", complete);
      command("set duration-months 120 go"); unchanged("duration edit cannot unfreeze", complete);
      defaults(); unchanged("Reset clears completed state and solutions", initial);
      System.out.println("PASS: " + checks + " numeric, state, slot, land, intervention, boundary, and conservation assertions.");
    } finally { w.dispose(); }
  }
}
