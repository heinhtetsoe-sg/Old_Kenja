function btn_submit(cmd) {    
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return true ;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG107}');
    if (result == false) {
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
