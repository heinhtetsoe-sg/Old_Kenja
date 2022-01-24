function btn_submit(cmd) {
    if (cmd == 'list'){
        parent.right_frame.location.href='knjz010kindex.php?cmd=edit&year=' + document.forms[0].year.value;
    }
    if (cmd == 'copy') {
        var value = eval(document.forms[0].year.value) + 1;
        var message = '年度のデータを作成します。既に存在するデータは削除されます。';
        if (!confirm('{rval MSG101}\n' + value + message)) {
            alert('{rval MSG203}');
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
