function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}'))
            return false;
        else
            cmd = "";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//更新
function doSubmit(cmd) {
    var attribute1 = document.forms[0].selectdata;
    attribute1.value = "";
    if (cmd == 'update'){
        if (document.forms[0].grade_input.length==0 && document.forms[0].grade_delete.length==0) {
            alert('{rval MSG916}');
            return false;
        }
        sep = "";
        for (var i = 0; i < document.forms[0].grade_input.length; i++)
        {
            attribute1.value = attribute1.value + sep + document.forms[0].grade_input.options[i].value;
            sep = ",";
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
