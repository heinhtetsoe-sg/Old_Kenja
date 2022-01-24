function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    //日付入力チェック
    if (document.forms[0].DATE.value == "" || document.forms[0].STRT_DATE.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE.focus();
        return false;
    } else {
        //年月固定チェック
        var strtspl = document.forms[0].STRT_DATE.value.split('/');
        var endspl = document.forms[0].DATE.value.split('/');
        if (strtspl.length < 2 || endspl.length < 2) {
            alert('{rval MSG902}');
            if (strtspl.length < 2) {
                document.forms[0].STRT_DATE.focus();
            } else {
                document.forms[0].DATE.focus();
                }
            return false;
        }
        if (strtspl[0] != endspl[0] || strtspl[1] != endspl[1]) {
            alert('{rval MSG913}' + "対象年月は、同一月内で範囲設定してください。");
            document.forms[0].DATE.focus();
            return false;
        }
        //開始 > 終了チェック
        if (parseInt(strtspl[2], 10) > parseInt(endspl[2], 10)) {
            alert('{rval MSG913}' + "対象年月が不正です。");
            document.forms[0].DATE.focus();
            return false;
        }
    }

    if (document.forms[0].DATE.value > document.forms[0].LAST_DATE.value) {
        alert("対象月日が対象学期の期間を超えています。");
        document.forms[0].DATE.focus();
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
