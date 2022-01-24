function btn_submit(cmd) {
    if ((cmd == 'insertCsv' || cmd == 'csv') && document.forms[0].DIRECT_DEBIT.value == '') {
        alert('{rval MSG301}' + '\n(引落日)');
        return;
    }

    if (cmd == 'insertCsv') {
        var drectDebit = document.forms[0].DIRECT_DEBIT;
        for (var i = 0; i < drectDebit.length; i++) {
            if (drectDebit.options[i].selected && !drectDebit.options[i].text.match(/済/)) {
                drectDebit.options[i].text = drectDebit.options[i].text.trim() + '（済）';
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();

}
