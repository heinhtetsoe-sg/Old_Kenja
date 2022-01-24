function btn_submit(cmd) {
   
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}'))
            return false;
    }           
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == "all_edit") {
        window.open('knjp150kindex.php?cmd=all_edit&mode=ALL', '_top');
        return;
    }
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
