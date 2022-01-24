function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (cmd == 'edit_gakki') {
        cmd = 'edit';
        document.forms[0].TEST.value = "";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function calc(obj) {
    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = "";
        return;
    }
}

function doSubmit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
