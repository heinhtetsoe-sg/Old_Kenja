function btn_submit(cmd) {
    //コピー処理
    if (cmd == 'copy' && !confirm('{rval MSG102}')){
        return true;
    }
    if (cmd == 'delete2' && !confirm('{rval MSG103}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function btn_keypress(){
    if (event.keyCode == 13){
        event.keyCode = 0;
        window.returnValue  = false;
    }
}