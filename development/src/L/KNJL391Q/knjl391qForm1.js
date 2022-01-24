function btn_submit(cmd) {
    if (cmd == 'exec') {
        var i;
        var c_check = 0;
        var cnt = document.forms[0].CHECK_CNT.value;
        for (i = 0; i < cnt; i++) {
            if (document.getElementById("CHECK"+i).checked) {
                c_check = 1;
            }
        }
        if (c_check != 1) {
            alert('印刷するリストを選択してください。');
            return false;
        }
        if (document.forms[0].PLACE_COMB && document.forms[0].PLACE_COMB.value == '') {
            alert('試験会場を指定してください。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}
function newwin(SERVLET_URL){
    var i;
    var c_check = 0;
    var cnt = document.forms[0].CHECK_CNT.value;
    for (i = 0; i < cnt; i++) {
        if (document.getElementById("CHECK"+i).checked) {
            c_check = 1;
        }
    }
    if (c_check != 1) {
        alert('印刷するリストを選択してください。');
        return false;
    }
    if (document.forms[0].PLACE_COMB && document.forms[0].PLACE_COMB.value == '') {
        alert('試験会場を指定してください。');
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

