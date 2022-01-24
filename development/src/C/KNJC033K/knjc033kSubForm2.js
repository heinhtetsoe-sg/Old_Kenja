function doSubmit()
{
    if (document.forms[0].RCHECK1.checked && document.forms[0].APPOINTED_DAY.value =="") {
        alert ('{rval MSG301}' + '\n締め日を入力して下さい。');
        return false;
    }
    if (document.forms[0].RCHECK2.checked && document.forms[0].LESSON.value =="") {
        alert ('{rval MSG301}' + '\n授業時数を入力して下さい。');
        return false;
    }
    if (!document.forms[0].RCHECK1.checked && !document.forms[0].RCHECK2.checked) {
        alert ('{rval MSG301}' + '\n更新対象を指定して下さい。');
        return false;
    }
    if (document.forms[0].left_select.length == 0) {
        alert('{rval MSG916}' + '\n生徒を指定して下さい。');
        return;
    }

    attribute = document.forms[0].selectdata;
    attribute.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute.value = attribute.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'replace_update';
    document.forms[0].submit();
    return false;
}

function check_all(obj){
    var ii = 0;
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "RCHECK"+ii){
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}
