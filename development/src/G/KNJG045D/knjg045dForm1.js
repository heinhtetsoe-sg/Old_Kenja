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

    diary_date = document.forms[0].DIARY_DATE.value;
    if (cmd == 'shutcho') {
        loadwindow('knjg045dindex.php?cmd='+cmd+'&DIARY_DATE='+diary_date,1000,0,800,700);
        return true;
    } else if (cmd == 'kyuka') {
        loadwindow('knjg045dindex.php?cmd='+cmd+'&DIARY_DATE='+diary_date,1000,0,800,700);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function btn_submit2(cmd) {
    if (cmd == 'yotei') {
        if (document.forms[0].EVENT_NAME.value != "") {
            var Ev_txt1 = "\n";
            if (document.forms[0].NEWS.value == "") {
                Ev_txt1 = "";
            }
            Ev_txt1 += document.forms[0].EVENT_NAME.value;
            document.forms[0].NEWS.value += Ev_txt1;
        } else {
            alert('行事予定は登録されていません。');
            return false;
        }
    } 
    return false;
}