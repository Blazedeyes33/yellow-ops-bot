const http=require('http'),fs=require('fs'),path=require('path');const {chromium}=require('playwright');
const ROOT=__dirname;const MIME={'.html':'text/html','.js':'text/javascript','.png':'image/png','.f32':'application/octet-stream','.txt':'text/plain'};
const srv=http.createServer((req,res)=>{const p=path.join(ROOT,decodeURIComponent(req.url.split('?')[0]));fs.readFile(p,(e,d)=>{if(e){res.writeHead(404);res.end();return;}res.writeHead(200,{'Content-Type':MIME[path.extname(p)]||'application/octet-stream'});res.end(d);});});
(async()=>{await new Promise(r=>srv.listen(8766,r));
 const browser=await chromium.launch({headless:true,executablePath:'/opt/pw-browsers/chromium_headless_shell-1194/chrome-linux/headless_shell',args:['--use-gl=angle','--use-angle=swiftshader','--enable-unsafe-swiftshader','--ignore-gpu-blocklist']});
 fs.mkdirSync(path.join(ROOT,'renders/gameplay'),{recursive:true});
 for(const j of JSON.parse(fs.readFileSync(process.argv[2],'utf8'))){
  const page=await browser.newPage({viewport:{width:540,height:1200}});
  page.on('response',r=>{if(r.status()>=400)console.log('HTTP',r.status(),r.url());}); page.on('pageerror',e=>console.log('PAGEERR',j.out,e.message)); page.on('console',m=>{if(m.type()==='error')console.log('CONSOLE',j.out,m.text());});
  await page.goto('http://localhost:8766/gameplay.html?'+new URLSearchParams(j.q).toString());
  await page.waitForFunction(()=>window.READY||window.ERR,{timeout:60000}).catch(async()=>{console.log('TIMEOUT',await page.evaluate(()=>window.ERR||document.body.innerText.slice(0,200)));}); await page.evaluate(()=>window.render());
  fs.writeFileSync(path.join(ROOT,'renders/gameplay',j.out+'.png'),await page.screenshot({type:'png'})); console.log('OK',j.out); await page.close();
 }
 await browser.close();srv.close();})().catch(e=>{console.error(e);process.exit(1);});
