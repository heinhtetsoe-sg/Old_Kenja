function btn_submit(cmd) {

    var s_date = document.forms[0].S_DATE.value; //入力された開始日
    var e_date = document.forms[0].E_DATE.value; //入力された終了日

    if (document.forms[0].S_DATE.value == '') {
        alert('開始日を指定して下さい。');
        return false;
    }
    if (document.forms[0].E_DATE.value == '') {
        alert('終了日を指定して下さい。');
        return false;
    }
    if (s_date > e_date) {
        alert("日付の大小が不正です。");
        return false;
    }

    if (cmd == 'csv' && !confirm('処理を開始します。よろしいでしょうか？')){
           return true;
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

