function btn_submit(cmd) {
    if (cmd == 'list') {
        var examyear = document.forms[0].ENTEXAMYEAR.value;
        var appdiv = document.forms[0].APPLICANTDIV.value;
        var testdiv = document.forms[0].TESTDIV.value;

        parent.right_frame.location.href='knjl3040index.php?cmd=edit&chFlg=1&ENTEXAMYEAR=' + examyear + '&APPLICANTDIV=' + appdiv + '&TESTDIV=' + testdiv;
    }

    //次年度コピー
    if (cmd == 'copy') {
        var value = eval(document.forms[0].ENTEXAMYEAR.value) + 1;
        var message = document.forms[0].ENTEXAMYEAR.value + '年度のデータから、' + value + '年度にデータをコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
