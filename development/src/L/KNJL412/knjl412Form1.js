function btn_submit(cmd) {
    //コピー
    if (cmd == 'copy') {
        if (!confirm('{rval MSG101}')){
            return false;
        }
        if (document.forms[0].THIS_YEAR_CNT.value > 0) {
            alert('対象年度にデータがあります。');
            return;
        }
        if (document.forms[0].PRE_YEAR_CNT.value == 0) {
            alert('前年度にデータがありません');
            return;
        }
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
