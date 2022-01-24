function btn_submit(cmd) {
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //更新
    if (cmd == 'update') {
        if (!document.forms[0].SCHREGNO.value) {
            alert('{rval MSG304}');
            return;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == '1') {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//斜線を入れるチェックボックス
function checkSlash(obj, targetName1, targetName2) {
    if (obj.checked == true) {
        document.forms[0][targetName1].disabled = true;
        document.forms[0][targetName2].disabled = true;
    } else {
        document.forms[0][targetName1].disabled = false;
        document.forms[0][targetName2].disabled = false;
    }
}
