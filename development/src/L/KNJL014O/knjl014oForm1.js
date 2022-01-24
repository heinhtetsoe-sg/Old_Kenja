function btn_submit(cmd) {
    if ((cmd == 'exec' || cmd == 'update') && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    if (cmd == 'exec' || cmd == 'update') {
        document.forms[0].btn_exec.disabled  = true;
        document.forms[0].btn_l015e.disabled = true;
        document.getElementById('marq_msg').style.color = '#FF0000';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
