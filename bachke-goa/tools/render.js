const http=require('http'),fs=require('fs'),path=require('path'),{execFileSync}=require('child_process');
const {chromium}=require('playwright');
const ROOT=__dirname;const MIME={'.html':'text/html','.js':'text/javascript','.glb':'model/gltf-binary','.json':'application/json'};
const srv=http.createServer((req,res)=>{const p=path.join(ROOT,decodeURIComponent(req.url.split('?')[0]));fs.readFile(p,(e,d)=>{if(e){res.writeHead(404);res.end();return;}res.writeHead(200,{'Content-Type':MIME[path.extname(p)]||'application/octet-stream'});res.end(d);});});
(async()=>{await new Promise(r=>srv.listen(8765,r));
 const jobs=JSON.parse(fs.readFileSync(process.argv[2],'utf8'));
 const browser=await chromium.launch({headless:true,executablePath:'/opt/pw-browsers/chromium_headless_shell-1194/chrome-linux/headless_shell',args:['--use-gl=angle','--use-angle=swiftshader','--enable-unsafe-swiftshader','--ignore-gpu-blocklist']});
 for(const j of jobs){
  const page=await browser.newPage({viewport:{width:j.w||720,height:j.h||960}});
  const qs=new URLSearchParams({model:'models/'+j.model,w:j.w||720,h:j.h||960,...(j.anim?{anim:j.anim}:{}),view:j.view||'front',...(j.extra||{})}).toString();
  await page.goto('http://localhost:8765/view.html?'+qs);
  await page.waitForFunction(()=>window.READY||window.ERR,{timeout:120000});
  const err=await page.evaluate(()=>window.ERR);if(err){console.log('ERR',j,err);await page.close();continue;}
  const stats=await page.evaluate(()=>window.stats);
  fs.mkdirSync(path.join(ROOT,'renders',j.out),{recursive:true});
  if(j.frames){ // motion capture: deterministic stepping
    const fps=j.fps||30,dt=1/fps;const dur=j.seconds||stats.clipDuration*(j.cycles||2);const n=Math.round(dur*fps);
    await page.evaluate(()=>window.seek(0));
    for(let i=0;i<n;i++){await page.evaluate(d=>window.step(d),dt);
      const b=await page.screenshot({type:'png'});fs.writeFileSync(path.join(ROOT,'renders',j.out,`f${String(i).padStart(4,'0')}.png`),b);}
    const ff='/opt/pw-browsers/ffmpeg-1011/ffmpeg-linux';
    try{execFileSync(ff,['-y','-framerate',String(fps),'-i',path.join(ROOT,'renders',j.out,'f%04d.png'),'-c:v','libx264','-pix_fmt','yuv420p','-crf','18',path.join(ROOT,'renders',j.out+'.mp4')],{stdio:'ignore'});
        execFileSync(ff,['-y','-framerate',String(fps),'-i',path.join(ROOT,'renders',j.out,'f%04d.png'),'-vf','fps=15,scale=360:-1:flags=lanczos','-loop','0',path.join(ROOT,'renders',j.out+'.gif')],{stdio:'ignore'});}catch(e){console.log('ffmpeg fail',e.message);}
    console.log('MOTION',j.out,n,'frames',JSON.stringify({dur:stats.clipDuration}));
  } else {
    for(const v of (j.views||[j.view||'front'])){await page.evaluate(v=>window.setView(v),v);
      if(j.time!=null)await page.evaluate(t=>window.seek(t),j.time);else await page.evaluate(()=>window.step(0));
      const b=await page.screenshot({type:'png'});fs.writeFileSync(path.join(ROOT,'renders',j.out,`${v}.png`),b);}
    console.log('STILLS',j.out,JSON.stringify({rawHeight:stats.rawHeight,scale:stats.scale,bbox:stats.bboxRaw}));
  }
  await page.close();
 }
 await browser.close();srv.close();
})().catch(e=>{console.error(e);process.exit(1);});
