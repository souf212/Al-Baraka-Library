from pathlib import Path
import urllib.request
assets={
'src/main/resources/static/vendor/bootstrap.rtl.min.css':'https://raw.githubusercontent.com/twbs/bootstrap/v5.3.8/dist/css/bootstrap.rtl.min.css',
'src/main/resources/static/vendor/BOOTSTRAP-LICENSE':'https://raw.githubusercontent.com/twbs/bootstrap/v5.3.8/LICENSE',
'src/main/resources/static/fonts/Tajawal-Regular.ttf':'https://raw.githubusercontent.com/google/fonts/main/ofl/tajawal/Tajawal-Regular.ttf',
'src/main/resources/static/fonts/OFL.txt':'https://raw.githubusercontent.com/google/fonts/main/ofl/tajawal/OFL.txt'}
for name,url in assets.items():
 p=Path(name);p.parent.mkdir(parents=True,exist_ok=True)
 with urllib.request.urlopen(url,timeout=35) as response:p.write_bytes(response.read())
 print(name,flush=True)
