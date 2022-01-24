function btn_submit(cmd) 
{
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受検番号 )');
            return true;
        }
        var cflg = document.forms[0].cflg.value;
        if (vflg == true || cflg == 'true') {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

function OnClosing(){

    var cflg = document.forms[0].cflg.value;
    if (vflg == true || cflg == 'true') {
        if (confirm('{rval MSG108}')) {
            closeWin();
        }
    } else {
        closeWin();
    }
}