function btn_submit(cmd) {
    if(cmd == 'copy') {
        if (!confirm('{rval MSG101}')) {
            alert('{rval MSG203}');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
