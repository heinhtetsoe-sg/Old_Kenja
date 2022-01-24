function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";

    if (cmd == 'execute' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    if (document.forms[0].OUTPUT[0].checked == true) {
        if (document.forms[0].FILE.value == '') {
            alert('ファイルを指定してください');
            return false;
        }
    } else if (cmd != "") {
        cmd = "csv";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}