function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function calc(obj) {
    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = "";
        return;
    }
}

function doSubmit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    if (cmd != 'delete'){
        if (document.forms[0].DIV[0].checked && !confirm('科目以外の設定は、全て削除されます。(全学年)')){
            return false;
        }
        if (!document.forms[0].DIV[0].checked && !confirm('科目設定が削除されます。')){
            return false;
        }
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
