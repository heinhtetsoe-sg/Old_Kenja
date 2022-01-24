function btn_submit(cmd) 
{
    if (cmd == 'changeApp') {
        document.forms[0].EXAMNO.value = '';
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        var cflg = document.forms[0].cflg.value;
        if (vflg == true || cflg == 'true') {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }
    if (cmd == 'update' || cmd == 'back2' || cmd == 'next2') {
        if (document.forms[0].PROCEDUREDIV.value == '1' && document.forms[0].ENTDIV.value == '1') {
            if (document.forms[0].ENTER_COURSEMAJOR.value == '') {
                alert('{rval MSG310}\n入学区分が済みの場合は入学コースを選択して下さい。');
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_update.disabled = true;
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

//3桁カンマ区切り
function addFigure(str) {
    var num = new String(str).replace(/,/g, "");
    while(num != (num = num.replace(/^(-?\d+)(\d{3})/, "$1,$2")));
    return num;
}
