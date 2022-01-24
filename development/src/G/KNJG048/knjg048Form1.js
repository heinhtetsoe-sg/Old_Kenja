function btn_submit(cmd) {
    if (cmd == 'update') {
        //入力チェック
        if (document.forms[0].DIARY_DATE.value == "") {
            alert("日付が未入力です。");
            document.forms[0].DIARY_DATE.focus();
            return false;
        }
    } else if ((cmd == 'delete') && !confirm('{rval MSG103}')) {
        return true;
    }

    //入力チェック
    if (document.forms[0].DIARY_DATE.value == "") {
        alert("日付が未入力です。");
        document.forms[0].DIARY_DATE.focus();
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function btn_submit2(cmd) {
    if (cmd == 'yotei' && document.forms[0].EVENT_NAME.value != "") {
        var Ev_txt1 = "\n";
        if (document.forms[0].NEWS.value == "") {
            Ev_txt1 = "";
        }
        Ev_txt1 += document.forms[0].EVENT_NAME.value;
        document.forms[0].NEWS.value += Ev_txt1;
    }
    return false;
}