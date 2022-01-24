function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'subform1') {
        loadwindow('knjd131qindex.php?cmd=subform1',0,0,700,300);
        return true;
    } else if (cmd == 'subform2') {
        loadwindow('knjd131qindex.php?cmd=subform2',0,0,700,300);
        return true;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//一括更新画面へ
function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG308}');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    link = link + "&SCHREGNOS=" + top.main_frame.left_frame.document.forms[0].SCHREGNO.value;
    link = link + "&GRADE_HRCLASS=" + top.main_frame.left_frame.document.forms[0].GRADE.value;

    parent.location.href=link;
}
