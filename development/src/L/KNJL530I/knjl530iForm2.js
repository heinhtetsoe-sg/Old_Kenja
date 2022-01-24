// kanji=漢字
function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closeMethod() {
    top.main_frame.btn_submit('main');
    top.main_frame.closeit();
}
