function btn_submit(cmd) {
    if (cmd == "medical1_delete" && !confirm("{rval MSG103}")) {
        return true;
    }
    if (cmd == "medical1_insert" || cmd == "medical1_update") {
        if (document.forms[0].NAMECD.value == "" && document.forms[0].CENTER_NAME.value == "" && document.forms[0].ATTEND_STATUS.value == "") {
            alert("データを入力してください");
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}
