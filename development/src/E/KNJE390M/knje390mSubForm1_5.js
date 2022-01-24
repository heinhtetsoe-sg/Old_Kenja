function btn_submit(cmd) {
    if (cmd == "welfare1_delete" && !confirm("{rval MSG103}")) {
        return true;
    }
    if (cmd == "welfare1_update" || cmd == "welfare1_insert") {
        if (typeof document.forms[0].EQUIP2_SUPPLY_DATE != "undefined") {
            if (document.forms[0].EQUIP2_SUPPLY_DATE.value != "") {
                var regex = new RegExp(/^([1-9][0-9]{3})\/(0[1-9]{1}|1[0-2]{1})$/);
                if (!regex.test(document.forms[0].EQUIP2_SUPPLY_DATE.value)) {
                    alert("{rval MSG901}" + "年月を入力してください(例:2020/07)");
                    return true;
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function submitCheck(cmd, recordDiv, recordNo) {
    var recDiv = document.forms[0].RECORD_DIV.value;
    var recNo = document.forms[0].RECORD_NO.value;

    if (cmd == "welfare1_insert") {
        document.forms[0].RECORD_DIV.value = recordDiv;
        document.forms[0].RECORD_NO.value = recordNo;
    }
    if (cmd == "welfare1_update" || cmd == "welfare1_delete") {
        if (recDiv != recordDiv) {
            alert("{rval MSG308}");
            return false;
        }
        if (recordDiv == "1") {
            if (recNo != recordNo) {
                alert("{rval MSG308}");
                return false;
            }
        }
    }
    //
    btn_submit(cmd);
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}

//事前チェック
function preCheck(msg) {
    alert("{rval MSG305}\n(" + msg + ")");
}

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}
