function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'list') {
        window.open('knjz230index.php?cmd=sel&init=1','right_frame');
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].leftitem.length==0 && document.forms[0].rightitem.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].leftitem.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].leftitem.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = 'check';
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

