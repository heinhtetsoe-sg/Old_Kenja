function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd) {

    if (document.forms[0].YEAR.value == '') {
        alert('{rval MSG304}', '（年度）');
        return false;
    }
    if (document.forms[0].SEMES_ID.value == '') {
        alert('{rval MSG304}', '（学期）');
        return false;
    }
    if (document.forms[0].SCHOOL_KIND.value == '') {
        alert('{rval MSG304}', '（校種）');
        return false;
    }
    if (document.forms[0].COURSECD.value == '') {
        alert('{rval MSG304}', '（課程）');
        return false;
    }
    document.forms[0].cmd.value = cmd;

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
