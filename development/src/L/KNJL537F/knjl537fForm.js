function btn_submit(cmd) {

    if (cmd == 'insert' && !confirm('{rval MSG102}')){
        return false;
    }
    if(cmd == 'reset' && !confirm('{rval MSG106}')) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
