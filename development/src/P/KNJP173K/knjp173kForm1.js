function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//軽減額チェックボックス
function chkFlg(obj, div, locked) {

    if (locked == "1") {
        obj.checked = obj.checked ? false : true;
        alert('相殺済みのデータ変更は出来ません。');
        return false;
    }

    var monthArray = new Array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");
    var monthTotalArray = {"04" : 0, "05" : 0, "06" : 0, "07" : 0, "08" : 0, "09" : 0, "10" : 0, "11" : 0, "12" : 0, "01" : 0, "02" : 0, "03" : 0};
    var month = obj.value;

    planCancelFlg      = document.getElementById('DATA' + month + div + '_FLG');
    total              = document.getElementById('KEI' + div + '_DISP');

    totalMoneyDueDisp  = document.getElementById('TOTAL_KEIDUE_DISP');
    otherDiv = div == "DUE" ? "ADD" : "DUE";

    var monthCheck = false;
    var totalData = 0;
    for (i = 0; i < monthArray.length; i++) {
        moneyObj       = document.getElementById('DATA' + monthArray[i] + div + '_OBJ');
        moneyDisp      = document.getElementById('DATA' + monthArray[i] + div + '_DISP');
        cancelFlg      = document.getElementById('DATA' + monthArray[i] + div + '_FLG');

        if (cancelFlg == null) {
            continue;
        }

        if (monthArray[i] == month) {
            monthCheck = true;
        }
        if (monthCheck) {
            cancelFlg.checked = planCancelFlg.checked;
            if (!cancelFlg.checked) {
                moneyDisp.innerText = moneyObj.value ? number_format(String(moneyObj.value)) : "　";
            } else {
                moneyDisp.innerText = "　";
            }
        }

        if (!cancelFlg.checked) {
            totalData = totalData + parseInt(moneyObj.value ? moneyObj.value : 0);
            monthTotalArray[monthArray[i]] = parseInt(monthTotalArray[monthArray[i]]) + parseInt(moneyObj.value ? moneyObj.value : 0);
        }
    }
    total.innerText = number_format(String(totalData));

    for (i = 0; i < monthArray.length; i++) {
        moneyObj       = document.getElementById('DATA' + monthArray[i] + otherDiv + '_OBJ');
        cancelFlg      = document.getElementById('DATA' + monthArray[i] + otherDiv + '_FLG');

        if (cancelFlg == null) {
            continue;
        }

        if (!cancelFlg.checked) {
            totalData = totalData + parseInt(moneyObj.value ? moneyObj.value : 0);
            monthTotalArray[monthArray[i]] = String(parseInt(monthTotalArray[monthArray[i]]) + parseInt(moneyObj.value ? moneyObj.value : 0));
        }
    }
    totalMoneyDueDisp.innerText = number_format(String(totalData));

    for (var i in monthTotalArray) {
        totalMonthDisp = document.getElementById('TOTAL_DATA' + i + '_DISP');
        totalMonthDisp.innerText = number_format(String(monthTotalArray[i]));
    }
}

function OnNotUse(term)
{
    for (var i=0; i<document.forms[0].elements.length;i++){

        var e = document.forms[0].elements[i];

        if (e.type=='checkbox') {
            if (e.name!='caution_check'+term && e.name!='admonition_check'+term) {
                e.disabled = true;
            } 
        }
    }    
}