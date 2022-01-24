function btn_submit(cmd)
{
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    } else if (cmd == 'update') {
        //更新中の画面ロック
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            updateFrameLock();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkScore(obj, perfect) {
    obj.value = toInteger(obj.value);
    if (obj.value && Number(obj.value) > Number(perfect)) {
        obj.value = '';
        alert('得点は、' + perfect + 'までです。');
        obj.focus();
        return false;
    }
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

