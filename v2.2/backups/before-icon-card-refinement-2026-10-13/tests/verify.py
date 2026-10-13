"""V2.2 native model, layout, V2.1 resource regression and offline verification."""
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

ROOT = Path(__file__).resolve().parents[1]
MODEL = ROOT / 'models/ai-data-center-community-v2.2.nlogox'
RESULTS = ROOT / 'results'
PREVIOUS = ROOT.parent / 'v2.1'
SCENARIOS = ['baseline', 'expansion', 'recycled-water', 'solar', 'reforestation', 'combined', 'maximum']


def close(actual, expected):
    assert math.isclose(float(actual), float(expected), abs_tol=1e-8, rel_tol=1e-10), (actual, expected)


def records(path):
    with path.open(encoding='utf-8-sig', newline='') as source:
        rows = list(csv.reader(source))
    start = next(i for i, row in enumerate(rows) if row and row[0] == '[run number]')
    return [dict(zip(rows[start], row)) for row in rows[start + 1:] if row]


def structure():
    model = ET.parse(MODEL).getroot()
    view = model.find('widgets/view')
    assert [view.get(k) for k in ['maxPxcor', 'maxPycor', 'patchSize', 'fontSize']] == ['124', '54', '8.0', '16']
    widgets = list(model.find('widgets'))
    for i, node in enumerate(widgets):
        if node.tag in ['slider', 'chooser']:
            assert node.get('display') == node.get('variable')
        x, y, w, h = [int(node.get(k)) for k in ['x', 'y', 'width', 'height']]
        for other in widgets[i + 1:]:
            xx, yy, ww, hh = [int(other.get(k)) for k in ['x', 'y', 'width', 'height']]
            assert x+w <= xx or xx+ww <= x or y+h <= yy or yy+hh <= y, (node.tag, node.get('display'), other.tag, other.get('display'))
    buttons = {node.get('display'): node.text for node in widgets if node.tag == 'button'}
    assert buttons['Add Solar'] == 'enable-solution "Solar energy (40%)"'
    assert buttons['Recycle Water'] == 'enable-solution "Recycled water (50%)"'
    assert buttons['Reforest'] == 'enable-solution "Reforestation (12 months)"'
    for variable, default in [('starting-data-centers', '1'), ('starting-households', '10')]:
        slider = model.find(f"widgets/slider[@variable='{variable}']")
        assert [slider.get(k) for k in ['min', 'max', 'step', 'default']] == ['1', '50', '1', default]
    for name in ['house', 'facility', 'tree', 'sapling', 'solar', 'recycling', 'deer']:
        data = (ROOT / 'assets' / (name+'.png')).read_bytes()
        assert data[:8] == b'\x89PNG\r\n\x1a\n' and all(x > 0 for x in struct.unpack('>II', data[16:24]))
        assert data[25] in [4, 6], (name, 'Sprite must have an alpha channel')
        assert f'"../assets/{name}.png"' in model.findtext('code')
    assert len(list((ROOT/'assets/numbers').glob('*.png'))) == 56
    assert 'http' not in model.findtext('code').lower()
    print('PASS: widget layout/bindings, direct controls, 125×55 world, seven transparent sprites and 56 numeric glyphs.')


def scenarios():
    current = records(RESULTS/'scenarios.csv')
    previous = records(PREVIOUS/'results/scenarios.csv')
    assert len(current) == len(previous) == 511
    runs = {name: [] for name in SCENARIOS}
    changed_green = False
    for new, old in zip(current, previous):
        assert new.keys() == old.keys()
        runs[new['experiment-case']].append(new)
        for key in new:
            if key in ['green-coverage-percent', 'wellbeing-score', 'condition-for wellbeing-score']:
                continue
            try:
                float(old[key])
            except ValueError:
                assert new[key] == old[key], key
            else:
                close(new[key], old[key])
        changed_green |= not math.isclose(float(new['green-coverage-percent']),float(old['green-coverage-percent']))
        assert abs(float(new['balance-error-ml'])) < 1e-8
        for key in ['green-coverage-percent', 'water-supply-percent', 'wellbeing-score']:
            assert 0 <= float(new[key]) <= 100
        water, grid, green = [float(new[key]) for key in ['water-supply-percent', 'grid-demand-percent', 'green-coverage-percent']]
        score = .4*water + .3*max(0,100-grid) + .3*green
        close(new['wellbeing-score'],score)
        label = 'Healthy' if score >= 75 else 'Moderate' if score >= 50 else 'Stressed' if score >= 25 else 'Critical'
        assert new['condition-for wellbeing-score'] == label
    assert changed_green, 'New protected terrain must establish a new vegetation baseline'
    for name, run in runs.items():
        assert [int(float(row['ticks'])) for row in run] == list(range(73)), name
    for tick in range(73):
        close(runs['solar'][tick]['reservoir-ml'],runs['reforestation'][tick]['reservoir-ml'])
        if tick <= 24:
            for name in ['recycled-water','reforestation','combined']:
                for key in ['reservoir-ml','grid-demand-percent','green-coverage-percent']:
                    close(runs[name][tick][key],runs['solar'][tick][key])
        else:
            for name,key in [('recycled-water','reservoir-ml'),('solar','grid-demand-percent'),('reforestation','green-coverage-percent')]:
                close(runs[name][tick][key],runs['combined'][tick][key])
    close(runs['baseline'][0]['dc-request-ml'],.504)
    close(runs['baseline'][0]['household-request-ml'],.15)
    close(runs['baseline'][0]['grid-demand-percent'],2.85)
    close(runs['maximum'][0]['grid-demand-percent'],140.25)
    close(runs['maximum'][-1]['shortfall-ml'],5.35)
    final = [runs[name][-1] for name in SCENARIOS]
    with (ROOT/'docs/scenario-summary.csv').open('w',encoding='utf-8',newline='') as target:
        writer=csv.DictWriter(target,fieldnames=list(final[0])); writer.writeheader(); writer.writerows(final)
    print('PASS: 511 observations match V2.1 water/grid/count/allocation outputs; V2.2 vegetation and scores obey their new baseline.')
    return final


def plots(final):
    with (RESULTS/'baseline-plots.csv').open(encoding='utf-8-sig',newline='') as source:
        rows=list(csv.reader(source))
    pens=[]
    for i,row in enumerate(rows):
        if row and len(row)%4==0 and row == ['x','y','color','pen down?']*(len(row)//4):
            values=[]
            for point in rows[i+1:]:
                if not point or len(point)!=len(row) or not point[0]: break
                values.append(point)
            assert len(values)==73
            for offset in range(0,len(row),4):
                pen=[p[offset:offset+4] for p in values]
                for tick,p in enumerate(pen): close(p[0],tick/12)
                pens.append(pen)
    assert len(pens)==4
    expected=[float(final[0]['water-supply-percent']),2.85,100,float(final[0]['green-coverage-percent'])]
    for pen,number in zip(pens,expected): close(pen[-1][1],number)
    print('PASS: all native plot pens have 73 actual observations, elapsed years and the grid capacity reference.')


def main():
    parser=argparse.ArgumentParser(description=__doc__); parser.add_argument('--ui',action='store_true'); args=parser.parse_args()
    runtime=Path(os.environ.get('NETLOGO_HOME',ROOT.parent/'.tools/netlogo-7.0.4/PFiles64/NetLogo 7.0.4')).resolve()
    java=runtime/'runtime/bin/java.exe'
    assert java.is_file(), 'Set NETLOGO_HOME to NetLogo 7.0.4'
    RESULTS.mkdir(exist_ok=True)
    base=[str(java),'-Dfile.encoding=UTF-8',f'-Dnetlogo.extensions.dir={runtime / "extensions"}',f'-Dnetlogo.docs.dir={runtime}',f'-Dnetlogo.models.dir={runtime / "models"}','--class-path',str(runtime/'app/*')]
    def run(*arguments):
        result=subprocess.run(base+list(map(str,arguments)),cwd=ROOT.parent,capture_output=True,text=True,encoding='utf-8',timeout=300)
        assert result.returncode==0,result.stdout+result.stderr
        print(result.stdout.strip()); return result.stdout.strip()
    structure()
    numeric=run(ROOT/'tests/CheckModel.java',MODEL)
    run('org.nlogo.headless.Main','--model',MODEL,'--experiment','v2-scenarios','--threads','1','--table',RESULTS/'scenarios.csv')
    final=scenarios(); plots(final)
    with tempfile.TemporaryDirectory(prefix='offline copy ',dir=RESULTS) as temporary:
        relocated=Path(temporary).resolve(); assert relocated.is_relative_to(RESULTS.resolve())
        (relocated/'models').mkdir(); shutil.copy2(MODEL,relocated/'models'/MODEL.name); shutil.copytree(ROOT/'assets',relocated/'assets')
        offline=run(ROOT/'tests/CheckOffline.java',relocated/'models'/MODEL.name)
    gui=run(ROOT/'tests/CheckApp.java',MODEL,ROOT/'docs/screenshots') if args.ui else 'Not run; use --ui'
    evidence={'version':'2.2','model_sha256':hashlib.sha256(MODEL.read_bytes()).hexdigest(),'runtime':'NetLogo 7.0.4 / Java 17',
              'numeric':numeric,'offline':offline,'native_gui':gui,'scenario_observations':511,
              'resource_regression':'All monthly water/grid/count/allocation outputs match preserved V2.1.',
              'assets':{str(p.relative_to(ROOT/'assets')):hashlib.sha256(p.read_bytes()).hexdigest() for p in (ROOT/'assets').rglob('*.png')},
              'final_scenarios':final}
    (ROOT/'docs/verification-results.json').write_text(json.dumps(evidence,indent=2)+'\n',encoding='utf-8')
    print('PASS: V2.2 checks complete; separate evidence saved under v2.2/docs/.')


if __name__=='__main__': main()
