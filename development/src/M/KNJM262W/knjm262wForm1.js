function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//日付変更
function tmp_list(cmd, submit) {
    sdate = document.forms[0].SDATE.value;
    edate = document.forms[0].EDATE.value;
    if (!ckDate(sdate) || !ckDate(edate)) {
        alert("受付日範囲の日付の形式が不正です");
        return false;
    }

    document.forms[0].cmd.value = cmd;
    if (submit == "on") {
        document.forms[0].submit();
        return false;
    }
}

function ckDate(strDate) {
    if (!strDate.match(/^\d{4}\/\d{2}\/\d{2}$/)) {
        return false;
    }
    var y = strDate.split("/")[0];
    var m = strDate.split("/")[1] - 1;
    var d = strDate.split("/")[2];
    var date = new Date(y, m, d);
    if (date.getFullYear() != y || date.getMonth() != m || date.getDate() != d) {
        return false;
    }
    return true;
}

//印刷
function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
