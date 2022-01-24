function btn_submit(cmd) {
    if (cmd == 'add' || cmd == 'update') {
        var attribute = document.forms[0].selectvalue;
        attribute.value = "";
        sep = "";
        if (document.forms[0].grouplist.length == 0 && document.forms[0].classlist.length == 0) {
            alert('{rval MSG916}');
            return;
        }
        for (var i = 0; i < document.forms[0].grouplist.length; i++)
        {
            attribute.value = attribute.value + sep + document.forms[0].grouplist.options[i].value;
            sep = ",";
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}

function OnAuthError()
{
    alert('{rval MZ0026}');
    closeWin();
}
