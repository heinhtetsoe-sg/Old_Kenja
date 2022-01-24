function btn_submit(cmd) {
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//入力チェック
function calcCheck(obj) {
    var fromVal = document.forms[0].FROM_VAL.value;
    var toVal   = document.forms[0].TO_VAL.value;
    //空欄
    if (obj.value == '') {
        return;
    }
    re = new RegExp("["+fromVal+"-"+toVal+"]");
    if (!obj.value.match(re)) { 
        alert('{rval MSG901}'+'「'+fromVal+'～'+toVal+'」を入力して下さい。');
        obj.value = "";
        return;
    }
}
// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj) {
    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    } else {
        e.keyCode = 9;
    }
}
