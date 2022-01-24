function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_SCORE.value == '1') {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    //コンボ変更
    if (cmd == 'read') {
        document.forms[0].S_EXAMNO.value = '';
        document.forms[0].E_EXAMNO.value = '';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    if (document.forms[0].APPLICANTDIV.valeu == '' ||
        document.forms[0].TESTDIV.valeu == '' ||
        document.forms[0].EXAMHALLCD.valeu == ''
    ) {
        alert('{rval MSG916}');
        return false;
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

function interviewAllOk() {
    changeScore();
    $('#dataTable select').val(1);
}

function changeScore(){
    document.forms[0].CHANGE_SCORE.value = '1';
}
