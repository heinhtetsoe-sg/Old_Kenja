var dragapproved=false
var minrestore=0
var initialwidth,initialheight
var ie5=document.all&&document.getElementById
var ns6=document.getElementById&&!document.all

function drag_drop(e){
if (ie5&&dragapproved&&event.button==1){
document.getElementById("dwindow").style.left=tempx+event.clientX-offsetx
document.getElementById("dwindow").style.top=tempy+event.clientY-offsety
}
else if (ns6&&dragapproved){
document.getElementById("dwindow").style.left=tempx+e.clientX-offsetx
document.getElementById("dwindow").style.top=tempy+e.clientY-offsety
}
}

function initializedrag(e){
offsetx=ie5? event.clientX : e.clientX
offsety=ie5? event.clientY : e.clientY
if (ie5)
document.getElementById("saver").style.display=''

tempx=parseInt(document.getElementById("dwindow").style.left)
tempy=parseInt(document.getElementById("dwindow").style.top)

dragapproved=true
document.onmousemove=drag_drop
}

function loadwindow(url,x,y,width,height){
var cw = ns6? window.innerWidth-20 : document.body.clientWidth;
if (x+width > cw){
	x = cw - width;
}
if (!ie5&&!ns6)
window.open(url,"","width=width,height=height,scrollbars=1")
else{
document.getElementById("dwindow").style.display=''
document.getElementById("dwindow").style.width=initialwidth=width
document.getElementById("dwindow").style.height=initialheight=height
document.getElementById("dwindow").style.left=x
document.getElementById("dwindow").style.top =y
document.getElementById("dwindow").style.border ="solid silver 2px"
//document.getElementById("dwindow").style.top=ns6? window.pageYOffset*1+3 : document.body.scrollTop*1+3
document.getElementById("cframe").src=url
}
}

function maximize(){
if (minrestore==0){
minrestore=1 //maximize window
document.getElementById("maxname").setAttribute("src","../../image/system/restore.gif")
document.getElementById("dwindow").style.width=ns6? window.innerWidth-20 : document.body.clientWidth
document.getElementById("dwindow").style.height=ns6? window.innerHeight-20 : document.body.clientHeight
}
else{
minrestore=0 //restore window
document.getElementById("maxname").setAttribute("src","../../image/system/max.gif")
document.getElementById("dwindow").style.width=initialwidth
document.getElementById("dwindow").style.height=initialheight
}
document.getElementById("dwindow").style.left=ns6? window.pageXOffset : document.body.scrollLeft
document.getElementById("dwindow").style.top=ns6? window.pageYOffset : document.body.scrollTop
}

function closeit(){
document.getElementById("dwindow").style.display="none"
}
function hiddenWin(url){
document.getElementById("dwindow").style.display="none"
document.getElementById("cframe").src=url
}


if (ie5||ns6)
document.onmouseup=new Function("dragapproved=false;document.onmousemove=null;document.getElementById('saver').style.display='none'")
