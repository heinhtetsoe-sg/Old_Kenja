function btn_submit(cmd) {

    if (cmd == 'entGrdHistDel' && !confirm('{rval MSG103}')){
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
