function btn_submit(cmd) {
    if (cmd == 'copy') {

        if (document.forms[0].yearDataCnt.value != 0) {
            alert('今年度データが存在しています。');
            return;
        }
        if (document.forms[0].lastYearDataCnt.value == 0) {
            alert('前年度データが存在していません。');
            return;
        }

        if (!confirm('{rval MSG101}')) {
            alert('{rval MSG203}');
            return;
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

function OnPreError(cd)
{
    alert('{rval MSG305}' + '\n('+cd+')');
    closeWin();
}
