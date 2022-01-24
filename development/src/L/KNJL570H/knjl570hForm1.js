function btn_submit(cmd) {
    if (cmd == 'csv') {
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert('{rval MSG310}' + '\n学校種別');
            return false;
        }
        if (document.forms[0].DISTINCT_ID.value == "") {
            alert('{rval MSG310}' + '\n入試判別');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
