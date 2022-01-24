function btn_submit(cmd) {
    //コピー確認
    if (cmd == "copy" && !confirm('{rval MSG101}')) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
