function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return false;
    }
    if (cmd != 'delete') {
        if (document.forms[0].DIV[0].checked && !confirm('科目以外の設定は、全て削除されます。(全学年)')){
            return false;
        }
        if (!document.forms[0].DIV[0].checked && !confirm('科目設定が削除されます。')) {
            return false;
        }
    }
    if (cmd == 'add' || cmd == 'update') {
        if (document.forms[0].PERFECT.value == "" && document.forms[0].PASS_SCORE.value == ""){
            alert('満点または合格点を入力してください。');
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')){
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
