function btn_submit(cmd) {
    if (cmd == 'sim' || cmd == 'decision' || cmd == 'clear') {
        if (document.forms[0].COURSE_CNT.value == 0) {
            alert('{rval MSG305}' + '\n コースが1件も登録されていません。');
            return false;
        }
        if (document.forms[0].MAJORCD.value == '') {
            alert('{rval MSG301}' + '\n ( 学科 )');
            return false;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}' + '\n ( 試験区分 )');
            return false;
        }
    }
    if ((cmd == 'sim' || cmd == 'decision' || cmd == 'clear') && !confirm('{rval MSG101}')) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL, prgId) {

    var chkFlg = false;
    var outputChks = document.forms[0]["OUTPUT_CHK[]"];
    if (outputChks) {
        for (i = 0; i < outputChks.length; i++) {
            if(outputChks[i].checked) {
                chkFlg = true;
            }
        }
    }
    if (!chkFlg) {
        alert('{rval MSG301}' + '\n1つ以上の出力対象コースにチェックをしてください。');
        return false;
    }

    document.forms[0].PRGID.value = prgId;

    console.log (document.forms[0].PRGID.value);
    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
