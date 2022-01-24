function btn_submit(cmd) {
    //削除ボタン押し下げ時
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    //取消ボタン押し下げ時
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//観点①～⑩へマウスを乗せた場合、観点名称をチップヘルプで表示
function ViewcdMousein(e, msg_no){
    var msg = "";
    if (msg_no==1) msg = document.forms[0].VIEWCD1.value;
    if (msg_no==2) msg = document.forms[0].VIEWCD2.value;
    if (msg_no==3) msg = document.forms[0].VIEWCD3.value;
    if (msg_no==4) msg = document.forms[0].VIEWCD4.value;
    if (msg_no==5) msg = document.forms[0].VIEWCD5.value;
    if (msg_no==6) msg = document.forms[0].VIEWCD6.value;
    if (msg_no==7) msg = document.forms[0].VIEWCD7.value;
    if (msg_no==8) msg = document.forms[0].VIEWCD8.value;
    if (msg_no==9) msg = document.forms[0].VIEWCD9.value;
    if (msg_no==10) msg = document.forms[0].VIEWCD10.value;

    x = event.clientX + document.body.scrollLeft;
    y = event.clientY + document.body.scrollTop;
    document.all("lay").innerHTML        = msg;
    document.all["lay"].style.position   = "absolute";
    document.all["lay"].style.left  = x+5;
    document.all["lay"].style.top   = y+10;
    document.all["lay"].style.padding    = "4px 3px 3px 8px";
    document.all["lay"].style.border     = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#ccffff";
}

function ViewcdMouseout(){
    document.all["lay"].style.visibility = "hidden";
}

//入力チェック
function calc(obj) {

    var str = obj.value;
    var nam = obj.name;

    //空欄
    if (str == '') { 
        return;
    }

    //英小文字から大文字へ自動変換
    if (str.match(/a|b|c/)) { 
        obj.value = str.toUpperCase();
        str = str.toUpperCase();
    }

    //観点1～10
    if (!str.match(/A|B|C/)) { 
        alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。');
        obj.value = "";
        obj.focus();
        background_color(obj);
        return;
    }
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
