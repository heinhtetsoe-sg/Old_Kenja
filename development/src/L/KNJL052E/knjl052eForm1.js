function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_SCORE.value == '1') {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    //コンボ変更
    if (cmd == 'read') {
        document.forms[0].S_EXAMNO.value = '';
        document.forms[0].E_EXAMNO.value = '';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeScore(){
    document.forms[0].CHANGE_SCORE.value = '1';
}

function henkan(obj, flg){
    var str = obj.value;
    var nam = obj.name;

    //空欄
    if (str == '') { 
        return;
    }

    //英小文字から大文字へ自動変換
    obj.value = str.toUpperCase();
    str = str.toUpperCase();

    //評価、視唱
    if (!str.match(/A|B|C|D/)) {
        alert('{rval MSG901}'+'「a, A, b, B, c, C, d, D」を入力して下さい。');
        obj.value = '';
        obj.focus();
        return;
    }

    if (flg == '1') {
        //次のテキストに移動
        var nextFocusFlg = false;
        var prevObjIndex = 0;
        $('#dataTable input').each(function(index, element){
            if (nextFocusFlg) {
                element.focus();
                return false;
            }
            if (obj.name == element.name) {
                nextFocusFlg = true;
                prevObjIndex = index - 1;
                prevObjIndex = prevObjIndex < 0 ? 0 : prevObjIndex;
            }
        });
    }
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var nextFocusFlg = false;
        var prevObjIndex = 0;
        $('#dataTable input').each(function(index, element){
            if (nextFocusFlg) {
                if (event.shiftKey) {
                    $('#dataTable input')[prevObjIndex].focus();
                    return false;
                } else {
                    element.focus();
                    return false;
                }
            }
            if (obj.name == element.name) {
                nextFocusFlg = true;
                prevObjIndex = index - 1;
                prevObjIndex = prevObjIndex < 0 ? 0 : prevObjIndex;
            }
        });
        if (nextFocusFlg) {
            if (event.shiftKey) {
                $('#dataTable input')[prevObjIndex].focus();
                return false;
            }
        }
    }
}
