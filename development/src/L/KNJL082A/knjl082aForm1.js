function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
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

//チェックボックスon/off
function selectRowCtrl(receptno) {
    id = "ROWID-" + receptno;

    //選択行
    selectedRow = document.getElementById(id);

    //辞退フラグ
    var jitaiFlg = document.forms[0]["JITAI_FLG-" + receptno];

    //チェックon/off
    jitaiFlg.checked = !jitaiFlg.checked;

    //背景色
    color = "white";
    if (jitaiFlg.checked == true) {
        color = "yellow";
    }
    selectedRow.style.backgroundColor = color;
}
