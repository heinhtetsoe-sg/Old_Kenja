function btn_submit(cmd) {
    if (cmd == 'csv' && document.forms[0].APPLICANTDIV.value == '1') {
        if (document.forms[0].NOTICE_DATE.value == '') {
            alert('{rval MSG301}\n( 決定発行通知日 )');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
