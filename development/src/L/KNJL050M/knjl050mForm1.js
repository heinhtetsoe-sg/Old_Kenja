function btn_submit(cmd, score) {
    if (cmd == 'reference' || cmd == 'upBack' || cmd == 'upNext' || cmd == 'back' || cmd == 'next') {
        if (document.forms[0].RECEPTNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        if ((cmd == 'back' || cmd == 'next') &&
            score != document.forms[0].SCORE.value) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function kanryou() {
    if (!confirm('処理を完了します。よろしいでしょうか？')) {
        return true;
    }
    btn_submit('');
}

function alert_close() {
    if (!confirm('終了します。よろしいでしょうか？')) {
        return true;
    }
    closeWin();
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

//データチェック
function valCheck(score) {
    var checkVal = document.forms[0].TESTPAPERCD.value.split('-');
    if (parseInt(checkVal[2]) < score) {
        alert(checkVal[2] + "点満点です。");
    }
}

function zeroUme(keta, obj) {
    var num = new String(obj.value);
    var cnt = keta - num.length;

    if (cnt <= 0) {
        return num;
    }
    while (cnt-- > 0) {
        num = "0" + num;
    }
    obj.value = num;
}

//ウィンドウを開いたらテキストボックスにフォーカスを当てる
window.onload = function () {
    document.forms[0].SCORE.focus();
}


