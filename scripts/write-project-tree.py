from pathlib import Path
root=Path(__file__).resolve().parent.parent
ignored={'.tools','target','data','backups','.git','__pycache__'}
lines=['# Complete project structure','','Generated build/runtime directories are listed separately below.','','```text','albaraka/']
def tree(directory,prefix=''):
 children=sorted((p for p in directory.iterdir() if p.name not in ignored and not p.name.endswith('.log') and p.name not in {'PROJECT_TREE.md','SMOKE_RESULTS.json'}),key=lambda p:(not p.is_dir(),p.name))
 for i,p in enumerate(children):
  last=i==len(children)-1
  lines.append(prefix+('└── ' if last else '├── ')+p.name+('/' if p.is_dir() else ''))
  if p.is_dir():tree(p,prefix+('    ' if last else '│   '))
tree(root)
lines+=['```','','Generated / local-only:','','- `target/baraka-library.jar` — executable application.','- `target/surefire-reports/` — automated test evidence.','- `data/` — live database and optional uploaded logo.','- `backups/` — timestamped standalone SQLite backups.','- `.tools/` — optional portable Java/Maven, dependency cache and isolated smoke-test databases.','- `build.log` — latest build output.','- `SMOKE_RESULTS.json` — latest developer runtime smoke-test result.','- `PROJECT_TREE.md` — this file.','']
(root/'PROJECT_TREE.md').write_text('\n'.join(lines),encoding='utf-8')
