function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'reset') { //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    if (cmd == 'subform1'){
        loadwindow('knje012yindex.php?cmd=subform1',0,0,570,620);
        return true;
    }
    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function reloadIframe(url){
    document.getElementById("cframe").src=url
}
