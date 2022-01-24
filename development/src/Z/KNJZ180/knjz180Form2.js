function btn_submit(cmd) {
    if (cmd == "delete") {
        result = confirm("{rval MSG103}");
        if (result == false) {
            return true;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm("{rval MSG107}");
    if (result == false) {
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}
fcName = " "; //　フォーカス時のテキストフィールド名
function down() {
    if (event.keyCode == 13 && fcName == "HOLIDAY") {
        //        document.forms[0].cmd.value = 'edit';
        return false;
    }
    return true;
}

window.document.onkeypress = down;
