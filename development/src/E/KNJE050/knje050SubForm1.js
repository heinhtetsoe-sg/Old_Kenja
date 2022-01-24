function btn_submit(cmd) {

    if (cmd == 'main'){
        document.forms[0].cmd.value = "";
        document.forms[0].submit();
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
