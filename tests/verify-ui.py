"""Run after capturing ui-before/after-*.csv with the four built-in experiments."""
import csv
import math
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def rows(path):
    with path.open(encoding="utf-8-sig", newline="") as source:
        records = list(csv.reader(source))
    header = next(i for i, row in enumerate(records) if row and row[0] == "[run number]")
    result = {}
    for values in records[header + 1:]:
        if not values:
            continue
        record = dict(zip(records[header], values))
        key = tuple(record.get(k, "") for k in ("scenario", "water-allocation-policy", "seed-value"))
        assert key not in result, f"Duplicate scenario/policy/seed: {key}"
        result[key] = record
    return result


def verify():
    count = 0
    for experiment in ("smoke-test", "scenario-comparison", "rainfall-uncertainty", "allocation-policy-check"):
        before = rows(ROOT / "results" / f"ui-before-{experiment}.csv")
        after = rows(ROOT / "results" / f"ui-after-{experiment}.csv")
        assert before.keys() == after.keys(), experiment
        for key, expected in before.items():
            actual = after[key]
            assert expected.keys() == actual.keys(), experiment
            for metric, value in expected.items():
                if metric == "[run number]":
                    continue
                try:
                    number = float(value)
                except ValueError:
                    assert value == actual[metric], (experiment, key, metric)
                else:
                    assert math.isclose(number, float(actual[metric]), rel_tol=1e-10, abs_tol=1e-6), (
                        experiment, key, metric, value, actual[metric])
            count += 1
    model = ET.parse(ROOT / "models" / "ai-data-center-water-energy.nlogox").getroot()
    view = model.find("widgets/view")
    assert [view.get(k) for k in ("minPxcor", "maxPxcor", "minPycor", "maxPycor")] == ["-16", "16", "-12", "12"]
    assert float(view.get("patchSize")) == 18
    widgets = list(model.find("widgets"))
    # NetLogo's GUI binds chooser/slider variables through their display names.
    for widget in widgets:
        if widget.tag in ("chooser", "slider"):
            assert widget.get("display").lower() == widget.get("variable").lower()
    for i, a in enumerate(widgets):
        ax, ay, aw, ah = (int(a.get(k)) for k in ("x", "y", "width", "height"))
        for b in widgets[i + 1:]:
            bx, by, bw, bh = (int(b.get(k)) for k in ("x", "y", "width", "height"))
            assert ax + aw <= bx or bx + bw <= ax or ay + ah <= by or by + bh <= ay, (a.tag, a.get("display"), b.tag, b.get("display"))
    print(f"PASS: {count} scenario/policy/seed results unchanged; world and widget layout verified.")


if __name__ == "__main__":
    verify()
