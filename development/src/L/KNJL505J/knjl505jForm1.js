function btn_submit(cmd) {
    if (cmd == 'csv') {
        //�K�{�`�F�b�N
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG310}\n( �w�Z��� )');
            return;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG310}\n( ������� )');
            return;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//���
function newwin(SERVLET_URL) {
    //�K�{�`�F�b�N
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( �w�Z��� )');
        return;
    }
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( ������� )');
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
