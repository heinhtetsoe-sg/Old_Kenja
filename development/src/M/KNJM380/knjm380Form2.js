function btn_submit(cmd) {
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (document.forms[0].SCHCNT.value != '' && document.forms[0].CHECKCNT.value != ''){
        document.forms[0].btn_update.disabled = false;
        document.forms[0].btn_reset.disabled  = false;
    }else if(document.forms[0].SCHCNT.value == document.forms[0].CHECKCNT.value){
        document.forms[0].btn_update.disabled = false;
        document.forms[0].btn_reset.disabled  = false;
    }else {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled  = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function check(obj){
    
    if (eval(document.forms[0].SCHCNT.value) < eval(document.forms[0].CHECKCNT.value)){
        alert('回数よりチェック用の\n\n値が大きいです。');
        document.forms[0].CHECKCNT.value = '';
        document.forms[0].CHECKCNT.focus();
    }
}
