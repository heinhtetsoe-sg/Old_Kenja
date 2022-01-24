function btn_submit(cmd) {
    //削除処理
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    } else if (cmd == 'delete') {
        var chk = document.all['CHECKED[]'];
        for (var i = 0; i < chk.length; i++) {
            if (chk[i].checked) break;
        }
        if (i == chk.length) {
            alert("チェックボックスが選択されていません。");
            return true;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
