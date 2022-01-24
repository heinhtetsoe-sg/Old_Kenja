function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        if (vflg == true) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }
    if (cmd == 'change_testdiv') {
        document.forms[0].EXAMNO.value = '';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

function OnClosing() {
    if (vflg == true) {
        if (confirm('{rval MSG108}')) {
            closeWin();
        }
    } else {
        closeWin();
    }
}
