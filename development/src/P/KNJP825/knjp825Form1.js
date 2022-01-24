function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    if (document.forms[0].OUTPUT[0].checked == true) {
    } else if (cmd != ""){
        cmd = "csv";
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
//同じ日付データが存在するとき
function dateConfirm() {
    if (confirm('同じ引落日のデータが存在します。\nデータ削除後、更新してよろしいでしょうか？')) {
        document.forms[0].cmd.value = 'execSecond';
    } else {
        document.forms[0].cmd.value = 'errorDel';
    }
    document.forms[0].submit();
    return;
}
