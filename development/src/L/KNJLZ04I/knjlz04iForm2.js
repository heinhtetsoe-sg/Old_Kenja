function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}' + '\n(入試区分)');
            return false;
        }
        if (document.forms[0].EXAMNO_FROM.value == '') {
            alert('{rval MSG301}' + '\n( 受験番号帯(開始番号) ');
            return false;
        }
        if (document.forms[0].EXAMNO_TO.value == '') {
            alert('{rval MSG301}' + '\n( 受験番号帯(終了番号) ');
            return false;
        }

    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
