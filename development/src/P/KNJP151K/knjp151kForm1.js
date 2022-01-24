function btn_submit(cmd) {
    if (cmd == 'all_update') {
        var msgdata = "";
        if (document.forms[0].TOTALCD.length == 0) {
            msgdata = '\n○更新対象費目';
        }
        if (document.forms[0].REPAY_SEQ.length == 0) {
            msgdata += '\n○返金対象';
        }
        if (document.forms[0].left_select.length == 0) {
            msgdata += '\n○対象者';
        }
        if (msgdata.length > 0) {
            alert('{rval MSG304}' + msgdata);
            return false;
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
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
