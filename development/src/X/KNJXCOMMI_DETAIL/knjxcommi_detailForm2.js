function btn_submit(cmd) {

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    if (cmd == 'clear'){
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
