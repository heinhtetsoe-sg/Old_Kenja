function btn_submit(cmd)
{
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }
    //CSV
    if (cmd == 'exec') {
        if (document.forms[0].IBYEAR.value == '') {
            alert('年度を指定してください');
            return false;
        }
        cmd = 'downloadCsv';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closeMethod() {
    closeWin();
}

function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

//観点①～⑤へマウスを乗せた場合、観点名称をチップヘルプで表示
function ViewcdMousein(e, msg){
    x = event.clientX+document.body.scrollLeft;
    y = event.clientY+document.body.scrollTop;
    document.getElementById('lay').innerHTML = msg;
    document.getElementById('lay').style.position = "absolute";
    document.getElementById('lay').style.left = x+5;
    document.getElementById('lay').style.top = y+10;
    document.getElementById('lay').style.padding = "4px 3px 3px 8px";
    document.getElementById('lay').style.border = "1px solid";
    document.getElementById('lay').style.visibility = "visible";
    document.getElementById('lay').style.background = "#ccffff";
}

function ViewcdMouseout(){
    document.getElementById('lay').style.visibility = "hidden";
}
