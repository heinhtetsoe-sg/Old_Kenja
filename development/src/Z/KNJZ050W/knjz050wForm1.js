function btn_submit(cmd) {
    if (cmd == 'list') {
        parent.right_frame.location.href='knjz050windex.php?cmd=edit&changeFlg=1';
    }

    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //次年度コピー
    if (cmd == 'copy') {
        var value = eval(document.forms[0].YEAR.value) + 1;
        var message = document.forms[0].YEAR.value + "年度のデータから、" + value + "年度にデータをコピーします。";
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
