function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].TESTDIV.disabled) || (document.forms[0].EXAMHALLCD.disabled)) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    if (cmd == 'next' || cmd == 'back') {
        //必須チェック
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（受験種別）');
            return true;
        }
        if (document.forms[0].EXAMHALLCD.value == ""){
            alert('{rval MSG304}'+ '\n（会場）');
            return true;
        }
    }

    //コンボ変更
    if (cmd == 'read') {
        document.forms[0].S_EXAMNO.value = '';
        document.forms[0].E_EXAMNO.value = '';
    }

    //CSV出力
    if (cmd == "csv" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//事前チェックエラー（評価）
function errorPreCheck() {
    alert('{rval MSG305}'+'\n評価が設定されていません。');
}

//評価チェック
function CheckValue(obj) {
    obj.value = toAlpha(obj.value);

    var val = document.forms[0].VAL_LIST.value.replace(/,/g, '|');
    var msg = document.forms[0].VAL_LIST.value;

    re = new RegExp(val);
    if (obj.value && !obj.value.match(re)) {
        alert('{rval MSG901}' + '\n' + msg + 'を入力してください。');
        obj.focus();
        obj.select();
        return;
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_EXAMHALLCD.value      = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;

    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].EXAMHALLCD.disabled       = true;
    document.forms[0].btn_back.disabled         = true;
    document.forms[0].btn_next.disabled         = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_EXAMNO.value.split(',');
        var index = setArr.indexOf(obj.id);
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var targetId = setArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = setArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (setArr.length - 1)) {
                index++;
            }
            var targetId = setArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i < (setArr.length - 1); i++) {
                    targetId = setArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }

        document.getElementById(targetId).focus();
        document.getElementById(targetId).select();
        return false;
    }
}
