function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
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

function changeFlg(obj, examno) {
    document.forms[0].CHANGE_FLG.value = '1';
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_EXAMCOURSECD.value = document.forms[0].EXAMCOURSECD.options[document.forms[0].EXAMCOURSECD.selectedIndex].value;
    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].EXAMCOURSECD.disabled = true;

    document.getElementById('ROWID' + examno).style.background = 'yellow';
    obj.style.background = 'yellow';

    //更新フラグ
    targetName = 'UPD_FLG_' + examno;
    targetObject = eval('document.forms[0]["' + targetName + '"]');
    if (targetObject) {
        targetObject.value = '1';
    }
}
