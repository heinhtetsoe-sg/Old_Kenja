function btn_submit(cmd) {
    //終了
    if (cmd == 'end') {
        closeWin();
    }

    //更新
    if (cmd == 'update') {
        if (!confirm('{rval MSG102}')) {
            return;
        }
    }

    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_end.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//チェックボックス押下
function checkExce(obj, key) {
    //入学手続き金
    if (obj.name == 'DEPOSIT_CHECK_' + key) {
        tergetObject = document.getElementById('DEPOSIT_DATE_' + key);
        if (tergetObject) {
            if (obj.checked == true) {
                tergetObject.disabled = false;
                tergetObject.value = document.forms[0].CTRL_DATE.value;
            } else {
                tergetObject.disabled = true;
                tergetObject.value = '';
            }
        }
    }

    //入学金
    if (obj.name == 'FEE_CHECK_' + key) {
        tergetObject = document.getElementById('FEE_DATE_' + key);
        if (tergetObject) {
            if (obj.checked == true) {
                tergetObject.disabled = false;
                tergetObject.value = document.forms[0].CTRL_DATE.value;
            } else {
                tergetObject.disabled = true;
                tergetObject.value = '';
            }
        }
    }

    //辞退
    if (obj.name == 'DECLINE_CHECK_' + key) {
        tergetObject = document.getElementById('DECLINE_DATE_' + key);
        if (tergetObject) {
            if (obj.checked == true) {
                tergetObject.disabled = false;
                tergetObject.value = document.forms[0].CTRL_DATE.value;
            } else {
                tergetObject.disabled = true;
                tergetObject.value = '';
            }
        }
        if (obj.checked == true) {
            document.getElementById('DEPOSIT_CHECK_' + key).disabled = true;
            document.getElementById('DEPOSIT_DATE_' + key).disabled = true;
            document.getElementById('FEE_CHECK_' + key).disabled = true;
            document.getElementById('FEE_DATE_' + key).disabled = true;
        } else {
            document.getElementById('DEPOSIT_CHECK_' + key).disabled = false;
            document.getElementById('DEPOSIT_DATE_' + key).disabled = false;
            document.getElementById('FEE_CHECK_' + key).disabled = false;
            document.getElementById('FEE_DATE_' + key).disabled = false;
        }
    }
}

function getElementByName(eleName) {
    for (var e = 0; e < document.forms[0].elements.length; e++) {
        if (document.forms[0].elements[e].name == eleName) {
            return document.forms[0].elements[e];
        }
    }
    return null;
}
