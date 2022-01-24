function btn_submit(cmd) {
    if (cmd == "exec" && !confirm("処理を開始します。よろしいでしょうか？")) {
        document.forms[0].cmd.value;
        return true;
    }
    if (cmd == "exec" && document.forms[0].OUTPUT[0].checked == false) {
        cmd = "csv";
    }
    if (cmd == "exec") {
        //データ取込
        if (document.forms[0].OUTPUT[0].checked == true) {
            //読み込み中は、実行ボタンはグレーアウト
            document.forms[0].btn_exec.disabled = true;
            document.getElementById("marq_msg").style.color = "#FF0000";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

//セキュリティーチェック
function OnSecurityError() {
    alert("{rval MSG300}" + "\n高セキュリティー設定がされています。");
    closeWin();
}
