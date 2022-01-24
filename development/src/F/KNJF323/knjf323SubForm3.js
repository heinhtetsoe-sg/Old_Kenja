function btn_submit(cmd) {
    //学籍番号チェック
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }
    //取消確認
    if (cmd == 'subform3_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    //コピー確認
    if (cmd == 'subform3_copy') {
        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//disabled
function OptionUse(obj) {
    if (obj.checked == true) {
        flg = false;
    } else {
        flg = true;
    }

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == 'TEXT'+obj.name.substring(5)) {
            document.forms[0].elements[i].disabled = flg;
        }
    }
}
