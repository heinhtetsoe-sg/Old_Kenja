function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkReceptRange() {
    start = document.forms[0].EXAMNO_FROM.value;
    end = document.forms[0].EXAMNO_TO.value;

    if (start != '' && end != '') {
        if (start > end) {
            alert('終了には開始以降の値を入力してください。');
            document.forms[0].EXAMNO_TO.value = '';
            return false;
        }
    }

    return true;
}

//印刷
function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + '/KNJL';
    document.forms[0].target = '_blank';
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
