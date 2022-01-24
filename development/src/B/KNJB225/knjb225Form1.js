function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //事前設定チェック
    if (document.forms[0].GRADE_COURSE.value == '') {
        alert('{rval MSG305}'+'\n( コース )');
        return;
    }
    //データを分割してセット
    var gc = document.forms[0].GRADE_COURSE.value.split("-");
    document.forms[0].GRADE.value       = gc[0];
    document.forms[0].COURSECD.value    = gc[1];
    document.forms[0].MAJORCD.value     = gc[2];
    document.forms[0].COURSECODE.value  = gc[3];

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
