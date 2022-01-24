function btn_submit(cmd) {

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    if (cmd == 'exec' && document.forms[0].OUTPUT[0].checked == false) {
        cmd = 'csv';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
