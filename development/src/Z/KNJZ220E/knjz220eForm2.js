function btn_submit(cmd) {
    if (cmd == "delete" && !confirm('{rval MSG103}')) {
        return false;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function calc(obj) {
    var val = obj.value;

    //空白入力は許す
    if (val == '') return val;

    //数値チェック
    val = toInteger(val);

    if (val != obj.value) {
        //整数でないなら空白にする
        val = "";
    }
    //範囲チェック
    else if (val < 1 || val > 99) {
        alert('{rval MSG914}\n算出比率は1～99までの数値で入力してください\n入力された値は削除されます');
        val = "";
    }

    return val;
}
