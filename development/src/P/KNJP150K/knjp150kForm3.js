function btn_submit(cmd) {
    if ((cmd == 'all_update' || cmd == 'all_delete') && document.forms[0].left_select.length==0) {
        alert('{rval MSG304}');
        return false;
    }
    if (cmd == 'all_update' &&
       (document.forms[0].CHECKED1.checked == false &&
        document.forms[0].CHECKED2.checked == false &&
        document.forms[0].CHECKED3.checked == false )) {
        alert('更新する項目を選択して下さい。');
        return false;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].TMP_PAID_MONEY.value      = document.forms[0].PAID_MONEY.value;
    document.forms[0].TMP_PAID_MONEY_DATE.value = document.forms[0].PAID_MONEY_DATE.value;
    document.forms[0].TMP_PAID_MONEY_DIV.value  = document.forms[0].PAID_MONEY_DIV.value;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function money_check(obj)
{
    paid_money  = (document.forms[0].PAID_MONEY.value != "")  ? parseInt(document.forms[0].PAID_MONEY.value, 10) : 0;
    repay_money = (document.forms[0].REPAY_MONEY.value != "")  ? parseInt(document.forms[0].REPAY_MONEY.value, 10) : 0;

    if (paid_money < repay_money) {
        alert('{rval MSG901}'+'\n'+'入金額、返金額');
        obj.value = '';
        return true;
    }
    return;
}

function SetVal(flg)
{
    if (money[document.forms[0].TOTALCD.options[document.forms[0].TOTALCD.selectedIndex].value] != undefined) {
        document.forms[0].PAID_MONEY.value = money[document.forms[0].TOTALCD.options[document.forms[0].TOTALCD.selectedIndex].value];
        document.forms[0].PAID_MONEY_DATE.value = mday[document.forms[0].TOTALCD.options[document.forms[0].TOTALCD.selectedIndex].value];
        if (document.forms[0].PAID_MONEY_DIV.options[1].value != undefined) {
            document.forms[0].PAID_MONEY_DIV.value = document.forms[0].PAID_MONEY_DIV.options[1].value;
        }
    }
    if (flg == 1) {
        document.forms[0].CHECKED1.checked = '';
        document.forms[0].CHECKED2.checked = '';
        document.forms[0].CHECKED3.checked = '';
        document.forms[0].REPAY_MONEY.value = '';
        document.forms[0].REPAY_MONEY_DATE.value = '';
        document.forms[0].REPAY_MONEY_DIV.value = '';
        document.forms[0].REMARK.value = '';
    }
    return;
}

function Checkdisabled(flg)
{
    if (flg == 1) {
        SetVal(2);
    }

    for (var i = 0; i < document.forms[0].elements.length; i++)
    {
        var div = document.forms[0].elements[i];
        if (div.name == 'radiodiv' && div.checked) {
            if (flg == 1) {
                document.forms[0].CHECKED1.checked = '';
                document.forms[0].CHECKED2.checked = '';
                document.forms[0].CHECKED3.checked = '';
            }

            if (div.value == "1") {
                document.forms[0].CHECKED1.disabled = "disabled";
                document.forms[0].PAID_MONEY.disabled = "disabled";
                document.forms[0].PAID_MONEY_DATE.disabled = "disabled";
                document.forms[0].btn_calen.disabled = "disabled";
                document.forms[0].PAID_MONEY_DIV.disabled = "disabled";
            } else {
                document.forms[0].CHECKED1.disabled = '';
                document.forms[0].PAID_MONEY.disabled = '';
                document.forms[0].PAID_MONEY_DATE.disabled = '';
                document.forms[0].btn_calen.disabled = '';
                document.forms[0].PAID_MONEY_DIV.disabled = '';
            }                
        }
    }

    return;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}



