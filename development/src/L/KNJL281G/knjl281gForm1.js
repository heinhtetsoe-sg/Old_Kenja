function btn_submit(cmd) {

    //重複は何もしない
    if (document.forms[0].tyoufuku.value == 1) {
        return false;
    }

    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (!confirm('{rval MSG108}')) {
            return false;
        }
        closeWin();
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//学籍番号チェック
function checkNo(obj) {
    document.forms[0].tyoufuku.value = 0;
    if (obj.value != "") {
        //数値チェック
        obj.value = toInteger(obj.value);
        //桁数チェック
        if (String(obj.value).length < 4) {
            alert('{rval MSG901}' + '\n桁数が不足しています。4桁入力して下さい。');
            obj.focus();
            return;
        }
        //重複チェック
        var examnos = document.forms[0].HID_EXAMNO.value.split(",");
        for (cnt = 0; cnt < examnos.length; cnt++) {
            var targetId  = examnos[cnt];
            var targetObj = document.getElementById(targetId);
            if (!targetObj) {
                continue;
            }
            if (obj.id !== targetId && obj.value == targetObj.value) {
                document.forms[0].tyoufuku.value = 1;
                alert('{rval MSG901}' + '\nデータが重複しています。');
                obj.focus();
                return;
            }
        }
        //重複チェック
        var schregnos = document.forms[0].OTHER_SCHREGNO.value.split(",");
        for (cnt = 0; cnt < schregnos.length; cnt++) {
            var targetVal  = schregnos[cnt];
            if (obj.value == targetVal) {
                document.forms[0].tyoufuku.value = 1;
                alert('{rval MSG901}' + '\nデータが重複しています。');
                obj.focus();
                return;
            }
        }
    }
}

function Setflg(obj) {
    change_flg = true;

    document.getElementById('ROWID' + obj.id).style.background = "yellow";
    obj.style.background = "yellow";
}
