// kanji=漢字

function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function setTestkindcd_Score_flg() {
    var subTestCd = document.forms[0].SUB_TESTCD;
    document.forms[0].TESTCD.value = subTestCd.value.substr(0, 4);
    document.forms[0].SCORE_FLG.value  = subTestCd.value.substr(5, 1);
}

function newwin(SERVLET_URL){
    setTestkindcd_Score_flg();

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
