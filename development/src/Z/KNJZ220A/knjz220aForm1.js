function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function calc(obj) {
    var val = obj.value;
    //数値チェック
    val = toInteger(val);

    //空白入力は許す
    if (val == '') return val;

    if (val != obj.value) {
        //整数でないなら空白にする
        val = "";
    }
    //範囲チェック
    else if (val < 1 || val > 100) {
        alert('{rval MSG914}\n算出比率は1～100までの数値で入力してください\n入力された値は削除されます');
        val = "";
    }

    return val;
}
