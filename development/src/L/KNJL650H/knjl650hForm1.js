function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
        closeWin();
    }

    //読込
    if (cmd == 'read') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }

    //実行
    if (cmd == "exec") {
        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "csvInput";
        } else {
            cmd = "csvOutput";
        }
    }

    //CSV取込
    if (cmd == "csvInput") {
        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }

    if (cmd == 'update' || cmd == "csvInput") {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//得点チェック
function checkScore(obj) {
    //満点チェック
    if (obj.value != '*') {
        obj.value = toInteger(obj.value);
        var perfect = document.forms[0].PERFECT.value;
        if (obj.value > eval(perfect)) {
            alert('{rval MSG901}' + '\n満点：' + perfect + '以下で入力してください。');
            obj.focus();
            return;
        }
    }
}

function changeFlg(obj) {
    document.forms[0].CHANGE_FLG.value = '1';

    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].TESTSUBCLASSCD.disabled = true;
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_RECEPTNO.value.split(',');
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
