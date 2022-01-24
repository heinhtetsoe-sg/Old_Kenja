function btn_submit(cmd) {
    if (cmd == 'list'){
        parent.right_frame.location.href='knjz041tindex.php?cmd=edit&year=' + document.forms[0].year.value;
    }
    if (cmd == 'copy') {
        var value = eval(document.forms[0].year.value) + 1;
        var message = document.forms[0].year.value + '年度のデータから、' + value + '年度に存在しないデータのみコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}
