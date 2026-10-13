import org.nlogo.headless.HeadlessWorkspace;

class UiViews {
  public static void main(String[] args) throws Exception {
    var workspace = HeadlessWorkspace.newInstance();
    try {
      workspace.open(args[0]);
      workspace.command("set-current-directory \"" + System.getProperty("user.dir").replace('\\', '/') + "\"");
      workspace.command("run-self-tests");
      String[] scenarios = {"Reference baseline", "Rapid growth + drought", "Efficiency + reuse"};
      for (int i = 0; i < scenarios.length; i++) {
        workspace.command("set scenario \"" + scenarios[i] + "\" setup-scenario");
        if (!workspace.report("water-service-display community-service-percent").equals("Ready—advance one month")) throw new AssertionError("Ready state");
        workspace.command("export-view \"results/ui-preset-" + i + "-setup.png\"");
        workspace.command("repeat 120 [go] export-view \"results/ui-preset-" + i + "-120.png\"");
      }
      workspace.command("set scenario \"Reference baseline\" load-scenario set maximum-data-centers 30 set initial-data-centers 30 setup export-view \"results/ui-30-facilities.png\"");
      if (((Double)workspace.report("count data-centers")).intValue() != 30) throw new AssertionError("30 facilities");
      if (!workspace.report("count turtles = 32 and length (remove-duplicates [list xcor ycor] of data-centers) = 30").equals(true)) throw new AssertionError("Agents/positions");
      workspace.command("ask water-sources [set stored-water-ml 0] update-visuals export-view \"results/ui-empty.png\"");
      if (((Double)workspace.report("reservoir-percent")) != 0) throw new AssertionError("Empty reservoir");
      for (String policy : new String[]{"proportional", "community-first", "data-center-first"}) {
        workspace.command("set scenario \"Rapid growth + drought\" setup-scenario set water-allocation-policy \"" + policy + "\" repeat 120 [go] export-view \"results/ui-" + policy + ".png\"");
        workspace.command("set scenario \"Reference baseline\" load-scenario set water-allocation-policy \"" + policy + "\" set wue-liters-per-kwh 0 set community-demand-ml-per-day 0 setup go");
        if (!workspace.report("community-service-percent = 100 and data-center-water-service-percent = 100 and water-shortage-ml = 0").equals(true)) throw new AssertionError("Zero demand " + policy);
      }
      workspace.command("export-view \"results/ui-zero-demand.png\" set months-elapsed 1");
      if (!workspace.report("water-service-display 99.999").equals("Demand met | 99.999%")) throw new AssertionError("Tolerance boundary");
      if (!workspace.report("water-service-display 99.998").toString().startsWith("Water shortfall")) throw new AssertionError("Shortfall boundary");
      System.out.println("PASS: presets/setup/month120, 30 positions, empty/full, zero demand/all policies, tolerance.");
    } finally { workspace.dispose(); }
  }
}
