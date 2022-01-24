function btn_submit(cmd) {

    if (cmd == "execute") {
        if (document.forms[0].GRD_DATE.value == "") {
            alert('異動日付を指定して下さい。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

