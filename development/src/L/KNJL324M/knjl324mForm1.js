function btn_submit(cmd) {
    if (cmd == 'csv') {
        if (!check_date()) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    if (!check_date()) {
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function check_date() {
    var date       = document.forms[0].DATE.value;
    var yuko_date  = document.forms[0].YUKO_DATE.value;
    var limit_date = document.forms[0].LIMIT_DATE.value;
    var limit_time = document.forms[0].LIMIT_TIME.value;

    //作成日付チェック
    if (date == "") {
        alert("作成日付が不正です。");
        document.forms[0].DATE.focus();
        return false;
    }

    //有効期間チェック
    if (yuko_date == "") {
        alert("有効期間が不正です。");
        document.forms[0].LIMIT_DATE.focus();
        return false;
    }

    //入学手続き期間チェック
    if (limit_date == "") {
        alert("入学手続き期間が不正です。");
        document.forms[0].DATE.focus();
        return false;
    }

    //時間入力チェック
    if (limit_time == "") {
        alert("時間が不正です。");
        document.forms[0].LIMIT_TIME.focus();
        return false;
    }

    //時間の範囲内チェック
    if((parseInt(limit_time) < 1) || (parseInt(limit_time) > 24)){
        alert("時間が範囲外です。\n（1～24） ");
        return;
    }

    return true;
}