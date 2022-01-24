function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == 'update') {
        if (!confirm('{rval MSG102}')) {
            return false;
        }
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
        closeWin();
    }

    //読込
    if (cmd == 'read') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }

    //実行
    if (cmd == "exec") {
        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "csvInput";
        } else {
            cmd = "csvOutput";
        }
    }

    //CSV取込
    if (cmd == "csvInput") {
        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }

    if (cmd == 'update' || cmd == "csvInput") {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeFlg(obj){
    document.forms[0].CHANGE_FLG.value = '1';
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled          = true;

//    document.getElementById('ROWID' + obj.id).style.background="yellow";
//    obj.style.background="yellow";
}
