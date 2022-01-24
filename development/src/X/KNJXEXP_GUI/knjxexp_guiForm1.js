function btn_submit(cmd) {
    if (cmd == "chg_grade"){
        parent.right_frame.location.href = document.forms[0].path.value+'&init=1';
        cmd = "list";
        document.forms[0].mode.value = "ungrd";
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