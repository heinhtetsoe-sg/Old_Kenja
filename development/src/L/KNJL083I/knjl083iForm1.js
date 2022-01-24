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

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function SetflgAll(obj) {
    var chks = document.getElementsByClassName("test-absence-elem");
    var i;
    for (i in chks) {
        if ('object' != typeof chks[i]) {
            continue;
        }
        if (chks[i].checked != obj.checked) {
            chks[i].checked = obj.checked;
            Setflg(chks[i]);
        }
    }
}

function Setflg(obj) {
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.value;
    document.forms[0].HID_EXAMHALLCD.value         = document.forms[0].EXAMHALLCD.value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].EXAMHALLCD.disabled       = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";

    //更新フラグ
    targetName = "UPD_FLG_" + obj.id;
    targetObject = eval("document.forms[0][\"" + targetName + "\"]");
    if (targetObject) {
        targetObject.value = '1';
    }
}
