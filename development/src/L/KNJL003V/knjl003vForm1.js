function btn_submit(cmd) {
    if (cmd == 'list') {
        parent.right_frame.location.href = 'knjl003vindex.php?cmd=edit&chFlg=1&YEAR=' + document.forms[0].YEAR.value;
    }

    //前年度コピー
    if (cmd == 'copy') {
        var value = eval(document.forms[0].YEAR.value) - 1;
        var message = value + '年度のデータから、' + document.forms[0].YEAR.value + '年度にデータをコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
