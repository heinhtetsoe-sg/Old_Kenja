function btn_submit(cmd) {
    //取消
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //削除
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    //確定
    if (cmd == 'kakutei') {
        if (document.forms[0].MAX_DATA_DIV.value == '') {
            alert('項目数を入力して下さい。');
            document.forms[0].MAX_DATA_DIV.focus();
           return true;
        }
        document.forms[0].setcnt.value = document.forms[0].MAX_DATA_DIV.value;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//項目数チェック
function checkNum(obj) {
    obj.value=toInteger(obj.value);
    if (obj.value > 6) {
        alert('{rval MSG914}'+'\n'+'項目数は6までです。');
        obj.focus();
    }
}

