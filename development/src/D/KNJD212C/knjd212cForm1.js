function btn_submit(cmd) {
    if (cmd == 'execute') {
        if (document.forms[0].GRADE.options.length == 0) {
            return false;
        }

        if (confirm('{rval MSG101}')) {
            document.all('marq_msg').style.color = '#FF0000';
        } else {
            return;
        }

        document.forms[0].btn_exec.disabled = true;
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
