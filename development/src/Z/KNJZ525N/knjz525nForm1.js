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

        if (!checkNum(document.forms[0].MAX_DATA_DIV)) {
            return false;
        }
        document.forms[0].setcnt.value = document.forms[0].MAX_DATA_DIV.value;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//項目数チェック
function checkNum(obj) {
    var inputVal =parseInt(toInteger(obj.value));
    var max_item_num = parseInt(document.forms[0].max_item_num.value);
    console.log(typeof(inputVal));
    console.log(typeof(max_item_num));
    if (inputVal > max_item_num) {
        alert('{rval MSG914}'+'\n'+'項目数は'+max_item_num+'までです。');
        obj.focus();
        return false;
    }
    return true;
}

