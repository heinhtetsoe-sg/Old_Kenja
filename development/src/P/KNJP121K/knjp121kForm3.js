function btn_submit(cmd) {
    if ((cmd == 'all_update') && document.forms[0].left_select.length==0) {
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
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].TMP_PAID_MONEY.value      = document.forms[0].PAID_MONEY.value;
    document.forms[0].TMP_PAID_MONEY_DATE.value = document.forms[0].PAID_MONEY_DATE.value;
    document.forms[0].TMP_PAID_MONEY_DIV.value  = document.forms[0].PAID_MONEY_DIV.value;
    document.forms[0].TMP_REPAY_MONEY.value     = document.forms[0].REPAY_MONEY.value;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function money_check1() {
    if (document.forms[0].PAID_MONEY.value != "" && document.forms[0].MONEY_DUE.value != "") {
        if (parseInt(document.forms[0].PAID_MONEY.value, 10) != parseInt(document.forms[0].MONEY_DUE.value, 10)) {
            alert('入金必要額と一致していません。');
            return true;
        }
    }
    return;
}
function SetVal(flg) {
    //入金欄に値をセット
    if (money[document.forms[0].TOTALCD.options[document.forms[0].TOTALCD.selectedIndex].value] != undefined) {
        document.forms[0].MONEY_DUE.value       = money[document.forms[0].TOTALCD.options[document.forms[0].TOTALCD.selectedIndex].value];
        document.forms[0].PAID_MONEY.value      = money[document.forms[0].TOTALCD.options[document.forms[0].TOTALCD.selectedIndex].value];
        document.forms[0].PAID_MONEY_DATE.value = mday[document.forms[0].TOTALCD.options[document.forms[0].TOTALCD.selectedIndex].value];
        if (document.forms[0].PAID_MONEY_DIV.options[1].value != undefined) {
            document.forms[0].PAID_MONEY_DIV.value = document.forms[0].PAID_MONEY_DIV.options[1].value;
        }
    }
    if (flg == 1) {
        document.forms[0].CHECKED1.checked  = '';
        document.forms[0].CHECKED2.checked  = '';
        document.forms[0].CHECKED3.checked  = '';
        document.forms[0].REPAY_MONEY.value = '';
        document.forms[0].REPAY_DATE.value  = '';
        document.forms[0].REPAY_DEV.value   = '';
        document.forms[0].REMARK.value      = '';
        //小分類を有する中分類なら、入金・返金ともに編集不可
        var disFlg = false;
        if ('1' == sFlg[document.forms[0].TOTALCD.options[document.forms[0].TOTALCD.selectedIndex].value]) {
            disFlg = true;
        }
        document.forms[0].CHECKED1.disabled         = disFlg;
        document.forms[0].MONEY_DUE.disabled        = disFlg;
        document.forms[0].PAID_MONEY.disabled       = disFlg;
        document.forms[0].PAID_MONEY_DATE.disabled  = disFlg;
        document.forms[0].btn_calen.disabled        = disFlg;
        document.forms[0].PAID_MONEY_DIV.disabled   = disFlg;
        document.forms[0].CHECKED2.disabled         = disFlg;
        document.forms[0].REPAY_MONEY.disabled      = disFlg;
        document.forms[0].REPAY_DATE.disabled       = disFlg;
        document.forms[0].btn_calen2.disabled       = disFlg;
        document.forms[0].REPAY_DEV.disabled        = disFlg;
    }
    return;
}

function Checkdisabled(flg) {
    if (flg == 1) {
        SetVal(2);
    }

    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var div = document.forms[0].elements[i];
        if (div.name == 'radiodiv' && div.checked) {
            if (flg == 1) {
                document.forms[0].CHECKED1.checked = '';
                document.forms[0].CHECKED2.checked = '';
                document.forms[0].CHECKED3.checked = '';
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
