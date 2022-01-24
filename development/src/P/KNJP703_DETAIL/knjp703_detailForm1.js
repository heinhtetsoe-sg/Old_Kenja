function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnParameterError()
{
    alert('{rval MSG304}');
    closeWin();
}
