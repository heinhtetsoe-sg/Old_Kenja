function btn_submit(cmd) {

    if (cmd == "copy" && !confirm('{rval MSG101}')){
        return false;
    }

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

