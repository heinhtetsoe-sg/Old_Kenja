function btn_submit(cmd) {
    
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (cmd == 'csv' && document.forms[0].category_selected.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function temp_clear()
{
    ClearList(document.forms[0].category_selected,document.forms[0].category_selected);
    ClearList(document.forms[0].majormaster,document.forms[0].majormaster);
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
