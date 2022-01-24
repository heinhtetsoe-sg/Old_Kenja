function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL) {
    var obj1 = document.forms[0].DATE;
    if (obj1.value == '') {
        alert('日付が不正です。');
        obj1.focus();
        return false;
    }

    if (document.forms[0].EDATE.value < document.forms[0].DATE.value || document.forms[0].DATE.value < document.forms[0].SDATE.value) {
        alert('指定範囲が学期外です。');
        obj1.focus();
        return;
    }

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL + '/KNJA';
    document.forms[0].target = '_blank';
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
