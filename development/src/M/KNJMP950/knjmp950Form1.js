function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    
    if (cmd == 'update' || cmd == 'insert') {
        if (document.forms[0].YEAR.value == "") {
            alert('{rval MSG301}' + '(年度)');
            return false;
        }
        if (document.forms[0].YOSAN_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(予算項目)');
            return false;
        }
        if (document.forms[0].REQUEST_GK.value == "") {
            alert('{rval MSG301}' + '(予算額)');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
