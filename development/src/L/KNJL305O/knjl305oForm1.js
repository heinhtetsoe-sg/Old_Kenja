function btn_submit(cmd) 
{
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

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


//ボタンを押し不可にする
function btn_disabled() {
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

//NO001
//得点データ社会又は、理科がある場合
function closing_window()
{

    alert('社会又は、理科の得点入力が\nありますので、この処理は実行できません。\nメンテナンス処理にて修正して下さい。');

    closeWin();
    return true;
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