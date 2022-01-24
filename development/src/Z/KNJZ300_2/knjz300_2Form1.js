function btn_submit(cmd) {
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

function doSubmit()
{
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].isGroup.length==0 && document.forms[0].noGroup.length==0) {
        alert("データは存在していません。");
        return false;
    }
    for (var i = 0; i < document.forms[0].isGroup.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].isGroup.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
