// 漢字
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
        applicd     = document.forms[0].APPLICATIONCD.value;
        moneydue    = document.forms[0].APPLI_MONEY_DUE.value;
        appdate     = document.forms[0].APPLIED_DATE.value;
        paidmoney   = document.forms[0].APPLI_PAID_MONEY.value;
        paiddate    = document.forms[0].APPLI_PAID_DATE.value;
        paidcls     = document.forms[0].APPLI_PAID_DIV.value;

        form_data = '&ALL_EDIT=1&APPLICATIONCD2='+applicd+'&APPLI_MONEY_DUE2='+moneydue+'&APPLIED_DATE2='+appdate+
                    '&APPLI_PAID_MONEY2='+paidmoney+'&APPLI_PAID_DIV2='+paidcls+'&APPLI_PAID_DATE2='+paiddate;

        window.open('knjp100kindex.php?cmd=all_edit'+form_data, '_top');
        return;
    }    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


