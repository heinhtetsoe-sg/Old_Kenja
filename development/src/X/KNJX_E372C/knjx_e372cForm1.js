/* Add by HPA for current_cursor start 2020/02/03 */
window.onload = function () {
    if (sessionStorage.getItem("KNJX_C031fForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJX_C031fForm1_CurrentCursor")).focus();
    }
}

function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[1].checked == true) {
        if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
            return true;
        }
    } else if (cmd != "" && cmd != "change_radio") {
        cmd = "csv";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
