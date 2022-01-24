function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){
    //必須チェック
    if (document.forms[0].GRADE.value == '') {
        alert('{rval MSG304}' + '学年が未選択状態です。');
        return false;
    }
    if (document.forms[0].KAIKIN_CD.value == '') {
        alert('{rval MSG304}' + '皆勤・精勤種別が未選択状態です。');
        return false;
    }

    //印刷
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

