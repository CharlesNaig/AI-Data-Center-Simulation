"""Validate V2 with the real NetLogo 7.0.4 runtime. Add --ui for native dialogs/screenshots."""
import argparse
import csv
import hashlib
import json
import math
import os
from pathlib import Path
import shutil
import struct
import subprocess
import tempfile
import xml.etree.ElementTree as ET

V2 = Path(__file__).resolve().parents[1]
MODEL = V2 / "models/ai-data-center-community-v2.nlogox"
RESULTS = V2 / "results"
SCENARIOS = ["baseline", "expansion", "recycled-water", "solar", "reforestation", "combined", "maximum"]
BACKUP = V2.parent / "v2/backups/2026-10-13-before-natural-layout"


def structural_checks():
    model = ET.parse(MODEL).getroot()
    widgets = list(model.find("widgets"))
    view = model.find("widgets/view")
    assert [view.get(k) for k in ("maxPxcor", "maxPycor", "patchSize", "fontSize")] == ["124", "56", "8.0", "16"]
    for i, widget in enumerate(widgets):
        if widget.tag in {"slider", "chooser"}:
            assert widget.get("display").lower() == widget.get("variable").lower(), "Native GUI binding"
        x, y, w, h = (int(widget.get(k)) for k in ("x", "y", "width", "height"))
        for other in widgets[i + 1:]:
            xx, yy, ww, hh = (int(other.get(k)) for k in ("x", "y", "width", "height"))
            assert x + w <= xx or xx + ww <= x or y + h <= yy or yy + hh <= y, (widget.tag, other.tag)
    for variable, default in [("starting-data-centers", "1"), ("starting-households", "10")]:
        slider = model.find(f"widgets/slider[@variable='{variable}']")
        assert [slider.get(k) for k in ("min", "max", "step", "default")] == ["1", "50", "1", default]
    assets = {}
    for name in ("cottage", "server", "deciduous-tree", "deer", "recycling", "solar-panel"):
        path = V2 / "assets" / f"{name}.png"
        data = path.read_bytes()
        assert data[:8] == b"\x89PNG\r\n\x1a\n" and struct.unpack(">II", data[16:24]) == (48, 48)
        assert f'"../assets/{name}.png"' in model.findtext("code")
        assets[name] = hashlib.sha256(data).hexdigest()
    assert "http" not in model.findtext("code").lower(), "Runtime code must use local assets"
    print("PASS: widget rectangles, native variable bindings, counts, View dimensions, and six local PNGs.")
    return assets


def read_scenarios():
    with (RESULTS / "scenarios.csv").open(encoding="utf-8-sig", newline="") as source:
        rows = list(csv.reader(source))
    start = next(i for i, row in enumerate(rows) if row and row[0] == "[run number]")
    runs = {name: [] for name in SCENARIOS}
    for values in rows[start + 1:]:
        if not values:
            continue
        row = dict(zip(rows[start], values))
        runs[row["experiment-case"]].append(row)
        assert abs(float(row["balance-error-ml"])) < 1e-8
        assert 0 <= float(row["water-supply-percent"]) <= 100
        assert 0 <= float(row["green-coverage-percent"]) <= 100
        assert 0 <= float(row["wellbeing-score"]) <= 100
    for name, rows in runs.items():
        assert [int(float(row["ticks"])) for row in rows] == list(range(73)), name
    def close(a, b):
        assert math.isclose(float(a), float(b), rel_tol=1e-10, abs_tol=1e-8), (a, b)
    for tick in range(73):
        for name in ("solar", "reforestation"):
            close(runs[name][tick]["reservoir-ml"], runs["solar"][tick]["reservoir-ml"])
        if tick <= 24:
            for name in ("recycled-water", "reforestation", "combined"):
                for metric in ("reservoir-ml", "grid-demand-percent", "green-coverage-percent"):
                    close(runs[name][tick][metric], runs["solar"][tick][metric])
        else:
            close(runs["recycled-water"][tick]["reservoir-ml"], runs["combined"][tick]["reservoir-ml"])
            close(runs["solar"][tick]["grid-demand-percent"], runs["combined"][tick]["grid-demand-percent"])
            close(runs["reforestation"][tick]["green-coverage-percent"], runs["combined"][tick]["green-coverage-percent"])
    close(runs["baseline"][-1]["reservoir-ml"], 39.346)
    close(runs["expansion"][12]["count data-centers"], 1)
    close(runs["expansion"][13]["count data-centers"], 20)
    close(runs["combined"][36]["reservoir-ml"], 33.72)
    close(runs["maximum"][0]["grid-demand-percent"], 140.25)
    close(runs["maximum"][-1]["grid-demand-percent"], 84.25)
    assert runs["combined"][-1]["condition-for wellbeing-score"] == "Healthy"
    assert runs["maximum"][-1]["condition-for wellbeing-score"] == "Stressed"
    assert float(runs["maximum"][-1]["shortfall-ml"]) > 0
    final = [runs[name][-1] for name in SCENARIOS]
    with (V2 / "docs/scenario-summary.csv").open("w", encoding="utf-8", newline="") as target:
        writer = csv.DictWriter(target, fieldnames=list(final[0]))
        writer.writeheader()
        writer.writerows(final)
    print("PASS: seven BehaviorSpace scenarios, 511 observations, causal isolation, real recovery and persistent shortage.")
    return final


def verify_plot_observations():
    with (RESULTS / "baseline-plots.csv").open(encoding="utf-8-sig", newline="") as source:
        rows = list(csv.reader(source))
    observations = []
    for i, row in enumerate(rows):
        if row and len(row) % 4 == 0 and row == ["x", "y", "color", "pen down?"] * (len(row) // 4):
            values = []
            for point in rows[i + 1:]:
                if not point or len(point) != len(row) or not point[0]:
                    break
                values.append(point)
            assert len(values) == 73
            for offset in range(0, len(row), 4):
                pen = [point[offset:offset + 4] for point in values]
                for tick, point in enumerate(pen):
                    assert math.isclose(float(point[0]), tick / 12, abs_tol=1e-10)
                observations.append(pen)
    assert len(observations) == 4  # Water, grid demand, capacity reference, vegetation.
    expected = [(100, 98.365), (2.85, 2.85), (100, 100), (99.20508744038156, 99.20508744038156)]
    for values, (first, last) in zip(observations, expected):
        assert math.isclose(float(values[0][1]), first, abs_tol=1e-8)
        assert math.isclose(float(values[-1][1]), last, abs_tol=1e-8)
    print("PASS: native plot exports contain 73 actual observations per pen, with years and a 100% capacity reference.")


def verify_layout_regression():
    def records(path):
        with path.open(encoding="utf-8-sig", newline="") as source:
            rows = list(csv.reader(source))
        start = next(i for i, row in enumerate(rows) if row and row[0] == "[run number]")
        return [dict(zip(rows[start], row)) for row in rows[start + 1:] if row]
    before = records(BACKUP / "scenarios-before.csv")
    after = records(RESULTS / "scenarios.csv")
    assert len(before) == len(after) == 511
    for old, new in zip(before, after):
        assert old.keys() == new.keys()
        for key, value in old.items():
            try:
                expected = float(value)
            except ValueError:
                assert new[key] == value, (key, value, new[key])
            else:
                assert math.isclose(float(new[key]), expected, abs_tol=1e-8, rel_tol=1e-10), (key, value, new[key])
    print("PASS: all 511 monthly observations match the original layout; only presentation changed.")


def verify_revert_backup():
    entries = json.loads((BACKUP / "manifest.json").read_text())
    for entry in entries:
        assert hashlib.sha256((BACKUP / "snapshot" / entry["path"]).read_bytes()).hexdigest() == entry["sha256"]
    with tempfile.TemporaryDirectory(prefix="restore test ", dir=RESULTS) as temporary:
        target = Path(temporary).resolve()
        assert target.is_relative_to(RESULTS.resolve())
        shutil.copytree(BACKUP / "snapshot", target, dirs_exist_ok=True)
        marker = b"Temporary newer design - must survive restore in the safety backup."
        (target / "README.md").write_bytes(marker)
        (target / "unrelated.txt").write_text("Preserve unrelated files.")
        completed = subprocess.run(["powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File",
                                    str(BACKUP / "Restore.ps1"), "-TargetV2", str(target)],
                                   capture_output=True, text=True, timeout=30)
        assert completed.returncode == 0, completed.stdout + completed.stderr
        for entry in entries:
            assert hashlib.sha256((target / entry["path"]).read_bytes()).hexdigest() == entry["sha256"]
        saved = list((target / "backups").glob("before-restore-*"))
        assert len(saved) == 1 and (saved[0] / "README.md").read_bytes() == marker
        assert (target / "unrelated.txt").read_text() == "Preserve unrelated files."
    print(f"PASS: restore script returns all {len(entries)} snapshot files byte-for-byte and preserves the replaced version and unrelated files.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ui", action="store_true")
    args = parser.parse_args()
    runtime = Path(os.environ.get("NETLOGO_HOME", V2.parent / ".tools/netlogo-7.0.4/PFiles64/NetLogo 7.0.4")).resolve()
    java = runtime / "runtime/bin/java.exe"
    if not java.is_file():
        raise SystemExit("Set NETLOGO_HOME to the NetLogo 7.0.4 installation folder.")
    RESULTS.mkdir(exist_ok=True)
    assets = structural_checks()
    base = [str(java), "-Dfile.encoding=UTF-8", f"-Dnetlogo.extensions.dir={runtime / 'extensions'}",
            f"-Dnetlogo.docs.dir={runtime}", f"-Dnetlogo.models.dir={runtime / 'models'}", "--class-path", str(runtime / "app/*")]
    def run(*arguments):
        completed = subprocess.run(base + list(map(str, arguments)), cwd=V2.parent, text=True, encoding="utf-8", capture_output=True, timeout=240)
        if completed.returncode:
            raise AssertionError(completed.stdout + completed.stderr)
        print(completed.stdout.strip())
        return completed.stdout.strip()
    numeric = run(V2 / "tests/CheckModel.java", MODEL)
    verify_plot_observations()
    run("org.nlogo.headless.Main", "--model", MODEL, "--experiment", "v2-scenarios", "--threads", "1", "--table", RESULTS / "scenarios.csv")
    final = read_scenarios()
    verify_layout_regression()
    verify_revert_backup()
    with tempfile.TemporaryDirectory(prefix="offline copy ", dir=RESULTS) as temporary:
        relocated = Path(temporary).resolve()
        assert relocated.is_relative_to(RESULTS.resolve())  # Bounds both copy and TemporaryDirectory cleanup.
        (relocated / "models").mkdir()
        shutil.copy2(MODEL, relocated / "models" / MODEL.name)
        shutil.copytree(V2 / "assets", relocated / "assets")
        offline = run(V2 / "tests/CheckOffline.java", relocated / "models" / MODEL.name)
    gui = run(V2 / "tests/CheckApp.java", MODEL, V2 / "docs/screenshots") if args.ui else "Not run; use --ui."
    report = {"model_sha256": hashlib.sha256(MODEL.read_bytes()).hexdigest(), "runtime": "NetLogo 7.0.4 / Java 17",
              "numeric": numeric, "offline": offline, "native_gui": gui, "scenario_observations": 511,
              "layout_regression": "PASS: all 511 observations match the original layout.",
              "revert_backup": "PASS: 32 original files restored byte-for-byte in temporary-copy test; replaced version and unrelated files preserved.",
              "asset_sha256": assets, "final_scenarios": final}
    (V2 / "docs/verification-results.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print("PASS: V2 verification complete; evidence saved under v2/docs/.")


if __name__ == "__main__":
    main()
