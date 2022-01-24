// kanji=漢字
function btn_submit(cmd)
{	
    document.forms[0].S_RECEPTNO.value = top.main_frame.document.forms[0].S_RECEPTNO.value;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
