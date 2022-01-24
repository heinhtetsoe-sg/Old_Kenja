function btn_submit(cmd)
{
    if (cmd == 'retParent') {
        window.opener.btn_submit('');
        closeWin();
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function IsUserOK_ToJump(URL, syoribi, period, class_staffcd, ccd)
{
    resizeTo(1024, 768);
    moveTo(0, 0);
    window.opener.IsUserOK_ToJump_1(URL, syoribi, period, class_staffcd, ccd);
}

function closeWindow()
{
    if(document.forms[0].chg.value == 'on')
        window.opener.document.forms[0].submit();
    closeWin();
}

function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CLASS_CHECK[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
