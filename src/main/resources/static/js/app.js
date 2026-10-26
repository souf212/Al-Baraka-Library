'use strict';
document.querySelectorAll('.sidebar nav a').forEach(a => {
 const path = window.location.pathname;
 if ((a.pathname === '/' && path === '/') || (a.pathname !== '/' && path.startsWith(a.pathname))) a.classList.add('active');
});
const menu=document.getElementById('menu-toggle');
menu?.addEventListener('click',()=>{const open=document.getElementById('navigation').classList.toggle('open');menu.setAttribute('aria-expanded',String(open));});
document.querySelectorAll('[data-back]').forEach(b=>b.addEventListener('click',()=>history.length>1?history.back():location.assign('/')));
document.querySelectorAll('form[data-confirm]').forEach(f=>f.addEventListener('submit',e=>{if(!window.confirm(f.dataset.confirm))e.preventDefault();}));
document.getElementById('copy-message')?.addEventListener('click',async()=>{
 const text=document.getElementById('message-text').textContent;
 try{if(navigator.clipboard && window.isSecureContext){await navigator.clipboard.writeText(text);}else{const range=document.createRange();range.selectNodeContents(document.getElementById('message-text'));const selection=window.getSelection();selection.removeAllRanges();selection.addRange(range);if(!document.execCommand('copy'))throw Error();selection.removeAllRanges();}document.getElementById('copy-status').textContent='تم نسخ الرسالة';}
 catch{document.getElementById('copy-status').textContent='تعذر النسخ. حدّد نص الرسالة وانسخه يدويًا.';}
});
document.querySelectorAll('.choice-search').forEach(input=>{
 let timer,request;
 input.addEventListener('input',()=>{clearTimeout(timer);timer=setTimeout(async()=>{
  request?.abort();request=new AbortController();const select=document.getElementById(input.dataset.select);
  try{const endpoint=input.dataset.select==='bookId'?'books':'borrowers';const response=await fetch('/loans/options/'+endpoint+'?q='+encodeURIComponent(input.value),{signal:request.signal});if(!response.ok)throw Error();const rows=await response.json();const current=select.selectedOptions[0];select.replaceChildren(new Option('اختر من نتائج البحث',''));if(current?.value)select.add(current);for(const row of rows)if(String(row.id)!==current?.value)select.add(new Option(row.label,String(row.id)));if(current?.value)select.value=current.value;}
  catch(e){if(e.name!=='AbortError')input.setCustomValidity('تعذر البحث؛ تحقق من اتصالك بالمكتبة وحاول مجددًا.');}input.addEventListener('input',()=>input.setCustomValidity(''),{once:true});
 },250);});
});
