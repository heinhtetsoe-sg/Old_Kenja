function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if (cmd == 'back' || cmd == 'now' || cmd == 'next') {
        if (document.forms[0].S_EXAMNO.value == '') {
            alert('{rval MSG301}' + ' \n( 受験番号 )');
        }
    }

    //更新
    if (cmd == 'update') {
        if (!confirm('{rval MSG102}')) {
            return false;
        }
    }

    if (cmd == 'update') {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chkEnableChgDisp(obj, cmd) {
    var oldVal = obj.value;
    var newVal = toInteger(obj.value);
    //チェック(NGなら""に変換)前後で同じ値なら、処理して問題無い
    if (oldVal == newVal) {
        if (document.forms[0].CHGFLG.value == "1") {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
        obj.value = newVal;
        btn_submit(cmd);
    } else {
        return false;
    }
    return true;
}

function chgChk(obj) {
    document.forms[0].CHGFLG.value = "1";
}

