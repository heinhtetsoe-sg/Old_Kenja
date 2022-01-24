function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
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
