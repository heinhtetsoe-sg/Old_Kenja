function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    if (document.forms[0].DATE.value == '') {
        alert("対象日付が未入力です。");
        return;
    }

    var date = document.forms[0].DATE.value;
        date = date.replace("/","-");
        date = date.replace("/","-");
    var sdate = document.forms[0].SDATE.value;
    var edate = document.forms[0].EDATE.value;
    if ((sdate > date) || (edate < date)) {
        sdate = sdate.replace("-","/");
        sdate = sdate.replace("-","/");
        edate = edate.replace("-","/");
        edate = edate.replace("-","/");
        alert('対象日付が入力範囲外です。\n（' + sdate + '～' + edate + '）');
        return true;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
