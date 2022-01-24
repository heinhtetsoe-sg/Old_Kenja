function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
        else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function disKouhiShihi(div) {
    //Œö”ï‚ÌŽž
    if (div == "1") {
        document.forms[0].GAKUNOKIN_DIV.disabled       = false;
        document.forms[0].REDUCTION_DIV.disabled       = false;
        document.forms[0].IS_REDUCTION_SCHOOL.disabled = false;
        document.forms[0].IS_CREDITCNT.disabled        = false;
        document.forms[0].IS_REPAY.disabled            = true;

    //Ž„”ï‚ÌŽž
    } else {
        document.forms[0].GAKUNOKIN_DIV.disabled       = true;
        document.forms[0].REDUCTION_DIV.disabled       = true;
        document.forms[0].IS_REDUCTION_SCHOOL.disabled = true;
        document.forms[0].IS_CREDITCNT.disabled        = true;
        document.forms[0].IS_REPAY.disabled            = false;
    }

    return;
}
function checkedMethod(obj) {
    for (var monthVal = 4; monthVal <= 15; monthVal++) {
        setMonth = monthVal > 12 ? monthVal - 12 : monthVal;
        document.forms[0]["COLLECT_MONTH_" + setMonth].checked = obj.checked;
    }
}
