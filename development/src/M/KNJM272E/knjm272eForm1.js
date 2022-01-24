function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closing_window() {
    alert('{rval MSG300}'+'\n'+'担当講座が無い為、権限がありません。');
    closeWin();
    return true;
}
//入力チェック
//名称マスタ「M003」ABBV2:画面 NAMECD2:テーブル
function check(obj) {
    var str = obj.value;
    var nam = obj.name;

    //空欄
    if (str == '') { 
        return;
    }

    //評価、再1～9
    var checkStr = document.forms[0].CHECK_HYOUKA.value.replace(/,/g, '|');
    var errStr   = document.forms[0].CHECK_HYOUKA.value.replace(/,/g, '、');
    re = new RegExp(checkStr);
    if (!String(str).match(re)) {
        alert('{rval MSG901}'+'「' + errStr + '」を入力して下さい。');
        obj.value = "";
        obj.focus();
        return;
    }
}
