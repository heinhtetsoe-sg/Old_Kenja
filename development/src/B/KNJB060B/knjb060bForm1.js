function btn_submit(cmd) {
    if (cmd == "csv") {
        if (check() == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    if (check() == false) {
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL + "/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function check() {
    //日付入力チェック
    if (document.forms[0].END_DATE.value == "" || document.forms[0].START_DATE.value == "") {
        alert("日付が不正です。");
        document.forms[0].END_DATE.focus();
        return false;
    } else {
        //年月固定チェック
        var strtspl = document.forms[0].START_DATE.value.split("/");
        var endspl = document.forms[0].END_DATE.value.split("/");
        if (strtspl.length < 2 || endspl.length < 2) {
            alert("{rval MSG902}");
            if (strtspl.length < 2) {
                document.forms[0].START_DATE.focus();
            } else {
                document.forms[0].END_DATE.focus();
            }
            return false;
        }
        //開始日付 > 終了日付
        var startDate = new Date(parseInt(strtspl[0]), parseInt(strtspl[1]) - 1, parseInt(strtspl[2]));
        var endDate = new Date(parseInt(endspl[0]), parseInt(endspl[1]) - 1, parseInt(endspl[2]));
        if (startDate > endDate) {
            alert("{rval MSG913}" + "\r\n変更時間割指定日付が不正です。");
            document.forms[0].END_DATE.focus();
            return false;
        }
    }
    return true;
}