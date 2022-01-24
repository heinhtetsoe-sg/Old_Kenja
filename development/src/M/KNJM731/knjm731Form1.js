function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//入力チェック
function Data_check(obj) {

    if (obj.value != "") {
        //数値チェック
        obj.value = toInteger(obj.value);

        //値チェック
        if (obj.value > "4") {
            alert('{rval MSG915}\n4回までです。');
            obj.focus();
        }
    }
    return;
}
