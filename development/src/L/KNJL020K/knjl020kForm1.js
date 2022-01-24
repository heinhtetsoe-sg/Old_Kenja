function btn_submit(cmd) {
    //削除処理
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
    //取消し処理
    if (cmd == 'cancel' && !confirm('{rval MSG106}')){
        return true;
    }
    //取消し処理
    if (cmd == 'testDivChange' && !confirm('{rval MSG105}')){
        return true;
    }
    //検索時のチェック
    if (cmd == 'search' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].ACCEPTNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        if(vflg == true){
            if(!confirm('{rval MSG108}')){
                return true;
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function copy(datadiv){
    loadwindow('knjl020kindex.php?cmd=dialog&datadiv='+datadiv,event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(),400,150);
}
function btn_disabled(){
    document.forms[0].btn_upd.disabled = true;
    document.forms[0].btn_preupd.disabled = true;
    document.forms[0].btn_nextupd.disabled = true;
    document.forms[0].btn_del.disabled = true;
}
function dispClear(){
    for (var i=0; i<document.forms[0].elements.length;i++){
        var el = document.forms[0].elements[i];
        if (el.type == "text"){
            el.value = '';
        }else if (el.type == "select-one" && !(el.name == "TESTDIV" || el.name == "SEX")){
            el.value = 0;
        }
    }
    document.getElementById("FS_UPDATED").innerText = "";
    document.getElementById("PS_UPDATED").innerText = "";
    document.getElementById("FS_ACCEPTNO").innerText = "";
    document.getElementById("PS_ACCEPTNO").innerText = "";
    document.getElementById("ACCEPTNO1").innerText = "";
    document.getElementById("ACCEPTNO2").innerText = "";

    document.forms[0].btn_copy1.disabled = true;
    document.forms[0].btn_copy2.disabled = true;
    document.forms[0].btn_upd.disabled = true;
    document.forms[0].btn_preupd.disabled = true;
    document.forms[0].btn_nextupd.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

//受付No（タイトル）へマウスを乗せた場合、受付Noの現在の登録値をチップヘルプで表示 2005/10/25 M.kuninaka
function AcceptnoMousein(e){
	var msg = "受付Noは｢" + document.forms[0].ACCEPTNO_MIN.value + "～" + document.forms[0].ACCEPTNO_MAX.value + "｣まで登録されています。";

	x = event.clientX+document.body.scrollLeft;
	y = event.clientY+document.body.scrollTop;
	document.all("lay").innerHTML = msg;
	document.all["lay"].style.position = "absolute";
	document.all["lay"].style.left = x+5;
	document.all["lay"].style.top = y+10;
	document.all["lay"].style.padding = "4px 3px 3px 8px";
	document.all["lay"].style.border = "1px solid";
	document.all["lay"].style.visibility = "visible";
	document.all["lay"].style.background = "#fffff0";
	//document.all["lay"].style.cursor = "hand";
}

function AcceptnoMouseout(){
    document.all["lay"].style.visibility = "hidden";
}
