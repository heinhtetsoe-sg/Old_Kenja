function btn_submit(cmd) {
    if (cmd == 'houkoku' || cmd == 'update') {
        if (document.forms[0].SCHOOLCD.value == "") {
            alert('教育委員会統計用学校番号が、未登録です。');
            return false;
        }
    }
    if (cmd == 'houkoku') {
        if (document.forms[0].FIXED_DATA.value == "") {
            alert('{rval MSG304}'+'(報告データ)');
            return false;
        }
        if (document.forms[0].DOC_NUMBER.value == "") {
            alert('{rval MSG304}'+'(文書番号)');
            return false;
        }
        if (document.forms[0].EXECUTE_DATE.value == "") {
            alert('{rval MSG304}'+'(報告日)');
            return false;
        }
        if (!confirm('{rval MSG108}')) return false;
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
//確定画面呼び出し
function fixed(REQUESTROOT) {

    load  = "loadwindow('"+ REQUESTROOT +"/F/KNJF307/knjf307index.php?cmd=fixedLoad";
    load += "',400,250,450,250)";

    eval(load);
}
