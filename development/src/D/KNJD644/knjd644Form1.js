function btn_submit(cmd) {
    if (cmd=="btn_clear") {
        document.forms[0].btn_keep.disabled = true;
        document.forms[0].btn_clear.disabled = true;
        document.forms[0].btn_end.disabled = false;
    }
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].CATEGORY_SELECTED.length==0 && document.forms[0].CATEGORY_NAME.length==0) {
        alert("データは存在していません。");
        return false;
    }
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
function AllClearList(OptionList, TitleName) 
{
        attribute = document.forms[0].CATEGORY_SELECTED;
        ClearList(attribute,attribute);
        attribute = document.forms[0].CATEGORY_NAME;
        ClearList(attribute,attribute);
}

