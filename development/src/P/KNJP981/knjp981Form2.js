function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
        else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function inoutChange() {

    var LEVY_IN_OUT_DIV = document.forms[0].LEVY_IN_OUT_DIV.value;
    //収入
    if (LEVY_IN_OUT_DIV == "1") {
        document.forms[0].ZATU_FLG.disabled = false;
        document.forms[0].YOBI_FLG.disabled = true;
        document.forms[0].KURIKOSI_FLG.disabled = false;
        if (document.forms[0].KURIKOSI_FLG.checked == true) {
            document.forms[0].KURIKOSI_MONEY.disabled = false;
        }
    //支出
    } else {
        document.forms[0].ZATU_FLG.disabled = true;
        document.forms[0].YOBI_FLG.disabled = false;
        document.forms[0].KURIKOSI_FLG.disabled = true;
        document.forms[0].KURIKOSI_MONEY.disabled = true;
    }
}
//繰越チェックボックス
function clicKurikosi(obj) {
    document.forms[0].KURIKOSI_MONEY.disabled = !obj.checked;
    document.forms[0].KURIKOSI_ALL.disabled   = !obj.checked;
}
