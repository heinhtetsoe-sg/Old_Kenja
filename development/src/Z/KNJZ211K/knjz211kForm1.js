function btn_submit(cmd) {
    if (cmd == "copy") {
        if (document.forms[0].copy_flg.value == 7) {
            if (!confirm('{rval MSG104}')) {
                alert("{rval MSG203}");
                return false;
            }
        } else if (document.forms[0].copy_flg.value == 5) {
            alert("{rval MSG101}");
        } else {
            alert("{rval MSG203}\n\n" + "前年度のデータがありません");
            return false;
        }
        parent.right_frame.location.href='knjz211kindex.php?cmd=edit';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
