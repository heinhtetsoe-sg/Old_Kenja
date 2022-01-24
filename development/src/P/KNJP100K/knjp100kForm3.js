// 漢字
function btn_submit(cmd) {
    if (cmd != 'change_class' && cmd != 'selectChange' && document.forms[0].left_select.length==0) {
        alert('{rval MSG304}');
        return false;
    }
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function SetMoney()
{
    if (money[document.forms[0].APPLICATIONCD.options[document.forms[0].APPLICATIONCD.selectedIndex].value] != undefined) {
        document.forms[0].APPLI_MONEY_DUE.value = money[document.forms[0].APPLICATIONCD.options[document.forms[0].APPLICATIONCD.selectedIndex].value];
    }
    document.forms[0].APPLIED_DATE.value = '';
    document.forms[0].APPLI_PAID_MONEY.value = '';
    document.forms[0].APPLI_PAID_DATE.value = '';
    document.forms[0].APPLI_PAID_DIV.value = '';
    return;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}



