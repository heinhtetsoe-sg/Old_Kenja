function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].TESTDIV.disabled) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//事前チェックエラー
function errorPreCheck() {
    alert('{rval MSG305}'+'\n合格類型が設定されていません。');
}

//値チェック
function CheckValue(obj) {
    var val = document.forms[0].VAL_LIST.value.replace(/,/g, '|');
    var msg = document.forms[0].VAL_LIST.value;

    re = new RegExp(val);
    if (obj.value && !obj.value.match(re)) {
        alert('{rval MSG901}' + '\n' + msg + 'を入力してください。');
        obj.focus();
        obj.select();
        return;
    }

    //名称表示
    var valArray = document.forms[0].VAL_LIST.value.split(',');
    var labelArray = document.forms[0].LABEL_LIST.value.split(',');
    var tmp = obj.name.split('_');
    if (obj.value == '') {
        document.getElementById('JUDGEMENT_NAME'+tmp[1]).innerHTML = '';
    } else {
        for (var i = 0; i < valArray.length; i++) {
            if (obj.value == valArray[i]) {
                document.getElementById('JUDGEMENT_NAME'+tmp[1]).innerHTML = labelArray[i];
                return;
            }
        }
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_SUB_ORDER.value       = document.forms[0].SUB_ORDER.options[document.forms[0].SUB_ORDER.selectedIndex].value;
    document.forms[0].HID_DESIREDIV.value       = document.forms[0].DESIREDIV.options[document.forms[0].DESIREDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].SUB_ORDER.disabled        = true;
    document.forms[0].DESIREDIV.disabled        = true;

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
