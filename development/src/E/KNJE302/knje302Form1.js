function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            //再読み込みを行い変更分を戻す
            cmd = "edit";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function link_submit(classCd, visitDate, seq) {

    document.forms[0]['EDIT_PRISCHOOLCD'].value = classCd;
    document.forms[0]['EDIT_VISIT_DATE'].value = visitDate;
    document.forms[0]['EDIT_SEQ'].value = seq;
    btn_submit('edit');
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

