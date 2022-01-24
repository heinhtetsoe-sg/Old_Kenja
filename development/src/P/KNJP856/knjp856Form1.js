function btn_submit(cmd) {
    var headOutputFlg = document.forms[0].OUTPUT[0].checked;
    var csvInputFlg = document.forms[0].OUTPUT[1].checked;
    var errOutput = document.forms[0].OUTPUT[2].checked;
    var execCopyFlg = document.forms[0].OUTPUT[3].checked;

    if (cmd == "csv") {
        if (headOutputFlg) {
            //ヘッダ出力
            cmd = "header";
        } else if (csvInputFlg) {
            //データ取込
            cmd = "exec";
        } else if (errOutput) {
            //エラー出力
            cmd = "err";
        } else if (execCopyFlg) {
            //前年度確定コピー
            cmd = "copy";
        }
    }

    if ((cmd == "exec" || cmd == "copy") && !confirm("処理を開始します。よろしいでしょうか？")) {
        document.forms[0].cmd.value;
        return true;
    }

    if (cmd == "exec" || cmd == "copy") {
        var e = document.getElementById("marq_msg");
        e.style.color = "#FF0000";
        e.style.fontWeight = "400";
        e.innerHTML = "処理中です...しばらくおまちください";

        //読み込み中は、実行ボタンはグレーアウト
        document.forms[0].btn_csv.disabled = true;
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
