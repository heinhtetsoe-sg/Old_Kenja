function btn_submit(cmd) {
    if (cmd == 'welfare1_delete' && !confirm('{rval MSG103}')){
        return true;
    }
    if (cmd == 'welfare1_update' || cmd == 'welfare1_insert') {
        if (typeof document.forms[0].SUPPLY_DATE != "undefined") {
            if (document.forms[0].SUPPLY_DATE.value != "") {
                var regex = new RegExp(/^([1-9][0-9]{3})\/(0[1-9]{1}|1[0-2]{1})$/);
                if (!regex.test(document.forms[0].SUPPLY_DATE.value)) {
                    alert('{rval MSG901}' + '年月を入力してください(例:2006/07)');
                    return true;
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}

//事前チェック
function preCheck(msg) {
    alert('{rval MSG305}\n('+msg+')');
}
