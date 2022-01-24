function btn_submit(cmd) {
    if (cmd == 'list') {
        var divVal;
        divVal = (document.forms[0].DIV1.checked == true) ? "1": "2";
        parent.right_frame.location.href='knjz452index.php?cmd=edit&chFlg=1&YEAR=' + document.forms[0].YEAR.value + '&SIKAKUCD=' + document.forms[0].SIKAKUCD.value + '&DIV=' + divVal;
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
