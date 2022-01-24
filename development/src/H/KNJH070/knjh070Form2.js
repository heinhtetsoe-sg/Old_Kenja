function btn_submit(cmd) {

    if ((cmd == 'update' || cmd == 'delete')  && (document.forms[0].TRAINDATE.value != document.forms[0].TRAINDATE.defaultValue)){
        alert('{rval MSG308}');
        return true;
    }else if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
