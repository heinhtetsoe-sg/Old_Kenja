function btn_submit(cmd) {

    //チェック
    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].SCHREGNO.value == ""){
            alert('{rval MSG304}');
            return true;
        } else if (document.forms[0].VISIT_DATE.value == "" || document.forms[0].VISIT_HOUR.value == "" || document.forms[0].VISIT_MINUTE.value == "") {
            alert('来室日時が入力されていません。\n　　　　（必須入力）');
            return true;
        }
   } else if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
   } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkDecimal(obj) {
    var decimalValue = obj.value
    var check_result = false;

    if (decimalValue != '') {
        //空じゃなければチェック
        if (decimalValue.match(/^[0-9]+(\.[0-9]+)?$/)) {
            check_result = true;
        }
    } else {
        check_result = true;
    }

    if (!check_result) {
        alert('数字を入力して下さい。');
        obj.value = '';
    }

    //正しい値ならtrueを返す
    return check_result;
}

