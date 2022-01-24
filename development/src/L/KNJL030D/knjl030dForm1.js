function btn_submit(cmd) {
    //会場追加で受験種別が空ならメッセージ表示
    if (cmd == 'halladd') {
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}' + '\n ( 受験種別 )');
            return false;
        } else {
            loadwindow('knjl030dindex.php?cmd=edit&mode=insert',300,300,370,170)
            return true;
        }
    }

    //削除処理
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    } else if (cmd == 'delete') {
        var chk = document.all['CHECKED[]'];
        for (var i=0;i<chk.length;i++) {
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

//全選択on/off
function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
