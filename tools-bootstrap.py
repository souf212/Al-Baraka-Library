from pathlib import Path
import urllib.request, zipfile, concurrent.futures
root = Path('.tools'); root.mkdir(exist_ok=True)
def fetch(item):
    name, url = item
    dest = root / (name+'.zip')
    urllib.request.urlretrieve(url, dest)
    with zipfile.ZipFile(dest) as z: z.extractall(root)
    print(name+' ready', flush=True)
with concurrent.futures.ThreadPoolExecutor() as pool:
    list(pool.map(fetch, [('jdk','https://aka.ms/download-jdk/microsoft-jdk-21.0.10-windows-x64.zip'), ('maven','https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip')]))
