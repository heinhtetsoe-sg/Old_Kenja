function btn_submit(cmd) {
    //終了
    if (cmd == 'end') {
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled)) {
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

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

//得点チェック
function CheckScore(obj, perfect) {
    if (obj.value != "*") {
        obj.value = toInteger(obj.value);
        if (obj.value > eval(perfect)) {
            alert('{rval MSG901}' + '\n満点：'+perfect+'以下で入力してください。');
            obj.focus();
            return;
        }
    }
}
