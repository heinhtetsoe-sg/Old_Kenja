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

function doSubmit() {
    var attribute1 = document.forms[0].selectdata;
    attribute1.value = "";
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
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}


function temp_clear()
{
    ClearList(document.forms[0].sectionyear,document.forms[0].sectionyear);
    ClearList(document.forms[0].sectionmaster,document.forms[0].sectionmaster);
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
