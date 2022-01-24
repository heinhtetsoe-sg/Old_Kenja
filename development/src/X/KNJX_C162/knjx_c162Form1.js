function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd) {
    if (document.forms[0].knjc162NenkanAttendance.value != "1") {
        var obj1 = document.forms[0].SDATE;
        if (obj1.value == "") {
            alert("日付が不正です。");
            obj1.focus();
            return false;
        }
        var obj2 = document.forms[0].EDATE;
        if (obj2.value == "") {
            alert("日付が不正です。");
            obj2.focus();
            return false;
        }

        var sday = document.forms[0].SDATE.value.replace(/\//g, ""); //印刷範囲日付
        var eday = document.forms[0].EDATE.value.replace(/\//g, ""); //印刷範囲日付
        var sdate = document.forms[0].SEME_SDATE.value.split("/"); //学期開始日付
        var edate = document.forms[0].SEME_EDATE.value.split("/"); //学期終了日付

        if (sday > eday) {
            alert("日付の大小が不正です。");
            return false;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;
    oldcmd = document.forms[0].cmd;
    document.forms[0].cmd.value = cmd;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJX";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = oldcmd;
}
