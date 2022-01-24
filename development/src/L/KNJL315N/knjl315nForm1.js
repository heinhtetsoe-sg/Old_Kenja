function btn_submit(cmd) {
    if (cmd == 'csv') {
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert('{rval MSG301}' + '\n入試制度');
            return false;
        }
        if (document.forms[0].TESTDIV.value == "") {
            alert('{rval MSG301}' + '\n入試区分');
            return false;
        }
        if (document.forms[0].SHDIV.value == "") {
            alert('{rval MSG301}' + '\n専併区分');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}