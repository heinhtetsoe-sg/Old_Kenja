//サブミット
function btn_submit(cmd) {
    if (cmd == "update" && !confirm("{rval MSG101}")) {
        return false;
    }

    if (cmd == "update") {
        if (document.forms[0].PAID_DATE.value == "") {
            alert("入金日を指定してください。");
            return;
        }
    }

    //読み込み中は、実行ボタンはグレーアウト
    document.forms[0].btn_upd.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
