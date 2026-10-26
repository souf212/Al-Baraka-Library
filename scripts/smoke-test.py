"""Developer verification against a real JAR and isolated .tools database; never sends WhatsApp.
Requires Python 3 and Java 21. Run from the project root after mvn verify.
"""
from pathlib import Path
import os, subprocess, time, urllib.request, urllib.parse, urllib.error, http.cookiejar, re, secrets, json, ctypes
from html.parser import HTMLParser

ROOT=Path(__file__).resolve().parent.parent
JAVA=ROOT/'.tools/jdk-21.0.10+7/bin/java.exe'
if not JAVA.exists(): JAVA=Path('java')
WORK=ROOT/'.tools'/('smoke-'+secrets.token_hex(4));WORK.mkdir(parents=True)
env=os.environ.copy();env.pop('DEBUG',None);env.update(BARAKA_DB=str(WORK/'library.db'),BARAKA_BACKUPS=str(WORK/'backups'),BARAKA_PORT='18080',BARAKA_BIND='127.0.0.1')
base='http://127.0.0.1:18080';jar=ROOT/'target/baraka-library.jar'
opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
log=None;process=None
class Inputs(HTMLParser):
 def __init__(self):super().__init__();self.values={}
 def handle_starttag(self,tag,attrs):
  a=dict(attrs)
  if tag=='input' and 'name' in a:self.values[a['name']]=a.get('value','')
def get(path):
 with opener.open(base+path,timeout=10) as r:return r.read().decode('utf-8'),r.url
def post(path,values):
 body,_=get(path);p=Inputs();p.feed(body);values['_csrf']=p.values['_csrf']
 with opener.open(base+path,urllib.parse.urlencode(values).encode(),timeout=20) as r:return r.read().decode('utf-8'),r.url
def start():
 global log,process
 log=open(WORK/'server.log','ab',buffering=0)
 process=subprocess.Popen([str(JAVA),'-Xms64m','-Xmx256m','-jar',str(jar)],cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT,creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
 for _ in range(100):
  if process.poll() is not None:raise RuntimeError('JAR exited: '+str(WORK/'server.log'))
  try:get('/login');return
  except (urllib.error.URLError,TimeoutError):time.sleep(.3)
 raise RuntimeError('Startup timeout')
def stop():
 global process,log
 if process and process.poll() is None:process.terminate();process.wait(timeout=20)
 if log:log.close()
def cli(*args):return subprocess.run([str(JAVA),'-jar',str(jar),*args],cwd=ROOT,env=env,capture_output=True,text=True,timeout=40)
results=[]
try:
 start();body,url=get('/login');assert '/setup' in url and 'lang="ar"' in body
 token=re.search(r'FIRST-RUN SETUP TOKEN \(local console only\): ([a-f0-9]+)',(WORK/'server.log').read_text(errors='replace')).group(1)
 password=secrets.token_urlsafe(22)
 body,url=post('/setup',dict(username='smokeadmin',fullName='مدير الاختبار',password=password,setupToken=token));assert '/login' in url
 post('/login',dict(username='smokeadmin',password=password));results.append('first-run setup and login')
 for asset in ['/vendor/bootstrap.rtl.min.css','/fonts/Tajawal-Regular.ttf','/css/app.css','/js/app.js']:
  with opener.open(base+asset) as r:assert r.status==200 and len(r.read())>1000
 body,url=post('/books/new',dict(title='كتاب اختبار التغليف',author='مؤلف',category='عام',totalCopies='1'));assert '/books/' in url
 book_id=url.rsplit('/',1)[1]
 body,url=post('/borrowers/new',dict(fullName='قارئ الاختبار',phoneNumber='0612345678',whatsappNumber='0612345678',whatsappConsent='true',membershipDate='2026-10-26',status='ACTIVE'));assert '/borrowers/' in url
 borrower_id=url.rsplit('/',1)[1]
 body,_=get('/loans/new');fields=Inputs();fields.feed(body)
 body,url=post('/loans/new',dict(bookId=book_id,borrowerId=borrower_id,loanDate=fields.values['loanDate'],dueDate=fields.values['dueDate']));assert '/loans/' in url
 loan_id=url.rsplit('/',1)[1];results.append('book, borrower and loan over HTTP; offline assets served')
 response=cli('--backup');assert response.returncode==0,response.stderr
 backup=next((WORK/'backups').glob('*.db'));results.append('live WAL-safe CLI backup')
 refused=cli('--restore',str(backup),'--confirm-replace');assert refused.returncode!=0;results.append('restore refused while app is running')
 # Sample actual Windows process working set, which includes JVM/native memory beyond the heap.
 class Counters(ctypes.Structure):
  _fields_=[('cb',ctypes.c_ulong),('PageFaultCount',ctypes.c_ulong)]+[(n,ctypes.c_size_t) for n in ['PeakWorkingSetSize','WorkingSetSize','QuotaPeakPagedPoolUsage','QuotaPagedPoolUsage','QuotaPeakNonPagedPoolUsage','QuotaNonPagedPoolUsage','PagefileUsage','PeakPagefileUsage']]
 memory=None
 if os.name=='nt':
  c=Counters();c.cb=ctypes.sizeof(c)
  ctypes.windll.psapi.GetProcessMemoryInfo.argtypes=[ctypes.c_void_p,ctypes.POINTER(Counters),ctypes.c_ulong]
  if ctypes.windll.psapi.GetProcessMemoryInfo(int(process._handle),ctypes.byref(c),c.cb):memory=round(c.WorkingSetSize/1024/1024,1)
 stop();start();post('/login',dict(username='smokeadmin',password=password));body,_=get('/loans/'+loan_id);assert 'كتاب اختبار التغليف' in body;results.append('records persist across process restart')
 # Perform a real return using its form's token.
 body,_=get('/loans/'+loan_id);fields=Inputs();fields.feed(body)
 with opener.open(base+'/loans/'+loan_id+'/return',urllib.parse.urlencode({'_csrf':fields.values['_csrf']}).encode()) as r:assert r.status==200
 stop()
 missing_confirmation=cli('--restore',str(backup));assert missing_confirmation.returncode!=0
 restored=cli('--restore',str(backup),'--confirm-replace');assert restored.returncode==0,restored.stderr
 start();post('/login',dict(username='smokeadmin',password=password));body,_=get('/loans/'+loan_id);assert 'تأكيد استلام الكتاب' in body;results.append('offline confirmed restore returns to pre-return snapshot')
 output=dict(checks=results,working_set_mb=memory,heap_limit_mb=256,isolated_directory=str(WORK))
 (ROOT/'SMOKE_RESULTS.json').write_text(json.dumps(output,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(output,ensure_ascii=True,indent=2))
finally:stop()
