function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//���
function newwin(SERVLET_URL) {
    //�K�{�`�F�b�N
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( �������x )');
        return;
    }
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( �����敪 )');
        return;
    }
    if (document.forms[0].EXAMHALLCD.value == '') {
        alert('{rval MSG310}\n( ���I�� )');
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

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
