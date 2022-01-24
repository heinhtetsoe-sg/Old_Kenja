function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')){
           return true;
    }

    if (cmd != 'main') {
        if (document.forms[0].OUTPUT[0].checked == true) cmd = "csv_header";
        if (document.forms[0].OUTPUT[1].checked == true) cmd = "exec";
        if (document.forms[0].OUTPUT[2].checked == true) cmd = "csv_error";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

