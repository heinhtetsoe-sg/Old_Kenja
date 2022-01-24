function btn_submit(cmd) {

    if (cmd == "clear") {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            alert('{rval MSG203}');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
