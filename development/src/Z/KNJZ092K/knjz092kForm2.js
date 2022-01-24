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

function changeTotal() {
    var money1 = document.forms[0].REDUCTIONMONEY_1.value == "" ? "0" : document.forms[0].REDUCTIONMONEY_1.value;
    var money2 = document.forms[0].REDUCTIONMONEY_2.value == "" ? "0" : document.forms[0].REDUCTIONMONEY_2.value;
    var setVal = parseInt(money1) + parseInt(money2);
    document.getElementById('MAX_MONEY').innerText = "  " + number_format(setVal);

    var minMoney1 = document.forms[0].MIN_MONEY_1.value == "" ? "0" : document.forms[0].MIN_MONEY_1.value;
    var minMoney2 = document.forms[0].MIN_MONEY_1.value == "" ? "0" : document.forms[0].MIN_MONEY_2.value;
    var minSetVal = parseInt(minMoney1) + parseInt(minMoney2);
    document.getElementById('MIN_MONEY').innerText = "  " + number_format(minSetVal);
}
