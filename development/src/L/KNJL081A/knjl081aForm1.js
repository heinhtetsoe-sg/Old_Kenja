function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//チェックボックスon/off
function selectRowCtrl(receptno) {
    id = "ROWID-" + receptno;

    //選択行
    selectedRow = document.getElementById(id);

    //払込フラグ
    var procedureDiv = document.forms[0]["PROCEDUREDIV-" + receptno];

    //チェックon/off
    procedureDiv.checked = !procedureDiv.checked;

    //背景色
    color = "white";
    if (procedureDiv.checked == true) {
        color = "yellow";
    }
    selectedRow.style.backgroundColor = color;
}
