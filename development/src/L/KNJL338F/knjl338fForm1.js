function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( 入試制度 )');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//disabled
function OptionUse(obj) {
    if(obj.value == "2" && obj.checked == true) {
        document.forms[0].RECEPTNO.disabled = false;
    } else {
        document.forms[0].RECEPTNO.disabled = true;
    }
    if(obj.value == "3" && obj.checked == true) {
        document.forms[0].RECEPTNO2S.disabled = false;
        document.forms[0].RECEPTNO2E.disabled = false;
    } else {
        document.forms[0].RECEPTNO2S.disabled = true;
        document.forms[0].RECEPTNO2E.disabled = true;
    }
    if(obj.value == "4" && obj.checked == true) {
        document.forms[0].RECEPTNO4S.disabled = false;
        document.forms[0].RECEPTNO4E.disabled = false;
    } else {
        document.forms[0].RECEPTNO4S.disabled = true;
        document.forms[0].RECEPTNO4E.disabled = true;
    }
}
