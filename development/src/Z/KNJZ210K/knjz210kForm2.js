function btn_submit(cmd) {
    if (cmd == 'chg_subclass' || cmd == 'add') {
        var attr_length = document.forms[0].selectlength;
        var attr_text = document.forms[0].selecttext;
        var attr_value = document.forms[0].selectvalue;
        attr_length.value = "";
        attr_length.value = document.forms[0].grouplist.length + "," + document.forms[0].classlist.length;
        attr_text.value   = "";
        attr_value.value  = "";
        sep = "";
        if (document.forms[0].grouplist.length != 0) {
            for (var i = 0; i < document.forms[0].grouplist.length; i++)
            {
                attr_text.value  =  attr_text.value  + sep + document.forms[0].grouplist.options[i].text;
                attr_value.value =  attr_value.value + sep + document.forms[0].grouplist.options[i].value;
                sep = ",";
            }
            for (i = 0; i < document.forms[0].classlist.length; i++)
            {
                attr_text.value  =  attr_text.value  + sep + document.forms[0].classlist.options[i].text;
                attr_value.value =  attr_value.value + sep + document.forms[0].classlist.options[i].value;
            }
        } else {
            document.forms[0].cmd.value = 'edit';
            document.forms[0].submit();
            return;
        }
    }
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

function Show_Confirm()
{
    if (!confirm("成績処理がされてます。変更しますか？\n変更する場合は成績処理を再度実行してください。")){
        document.forms[0].record_dat_flg.value = '0';
        document.forms[0].cmd.value = 'edit';
        document.forms[0].submit();
        return;
    } else {
        document.forms[0].record_dat_flg.value = '1';
        document.forms[0].cmd.value = 'update';
        document.forms[0].submit();
        return;
    }
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
