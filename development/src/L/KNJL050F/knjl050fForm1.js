function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == 'update' && document.all('SCORE[]') == undefined) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].APPLICANTDIV.value == '1') {
            if (document.forms[0].APPLICANTDIV.disabled || document.forms[0].TESTDIV.disabled || document.forms[0].TESTSUBCLASSCD.disabled) {
                if (confirm('{rval MSG108}')) {
                    closeWin();
                }
                return false;
            }
        } else {
            if (document.forms[0].APPLICANTDIV.disabled || document.forms[0].TESTDIV.disabled || document.forms[0].TESTSUBCLASSCD.disabled || document.forms[0].EXAMCOURSE.disabled || document.forms[0].TESTDIV0.disabled) {
                if (confirm('{rval MSG108}')) {
                    closeWin();
                }
                return false;
            }
        }
        closeWin();
    }

    //画面切換（前後）
    if (cmd == 'back' || cmd == 'next') {
        if (document.forms[0].APPLICANTDIV.value == '1') {
            if (document.forms[0].APPLICANTDIV.disabled || document.forms[0].TESTDIV.disabled || document.forms[0].TESTSUBCLASSCD.disabled || document.forms[0].EXAMCOURSE.disabled || document.forms[0].TESTDIV0.disabled) {
                if (!confirm('{rval MSG108}')) {
                    return false;
                }
            }
        } else {
            if (document.forms[0].APPLICANTDIV.disabled || document.forms[0].TESTDIV.disabled || document.forms[0].TESTSUBCLASSCD.disabled) {
                if (!confirm('{rval MSG108}')) {
                    return false;
                }
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if (confirm('{rval MSG106}')) return true;
    return false;
}

function MoveFocus(idx) {
    if (window.event.keyCode == 13) {
        idx++;
        document.all('SCORE' + idx.toString()).focus();
    }
}

//得点チェック
function CheckScore(obj) {
    if (obj.value != '*') {
        obj.value = toInteger(obj.value);
        if (obj.value > eval(aPerfect[obj.id])) {
            alert('{rval MSG901}' + '\n満点：' + aPerfect[obj.id] + '以下で入力してください。');
            obj.focus();
            return;
        }
    }
}

function Setflg(obj) {
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].TESTSUBCLASSCD.disabled = true;

    if (document.forms[0].APPLICANTDIV.value == '2') {
        document.forms[0].HID_TESTDIV0.value = document.forms[0].TESTDIV0.options[document.forms[0].TESTDIV0.selectedIndex].value;
        document.forms[0].HID_EXAMCOURSE.value = document.forms[0].EXAMCOURSE.options[document.forms[0].EXAMCOURSE.selectedIndex].value;

        document.forms[0].TESTDIV0.disabled = true;
        document.forms[0].EXAMCOURSE.disabled = true;
    }

    document.getElementById('ROWID' + obj.id).style.background = 'yellow';
    obj.style.background = 'yellow';
}

function Setflg2(obj, receptno) {
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].TESTSUBCLASSCD.disabled = true;

    document.getElementById('ROWID' + receptno).style.background = 'yellow';
    obj.style.background = 'yellow';
}
