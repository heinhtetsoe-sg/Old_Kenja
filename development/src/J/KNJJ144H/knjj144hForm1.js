function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[0].checked == false) {
        if (cmd != ""){
            cmd = "csv";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}