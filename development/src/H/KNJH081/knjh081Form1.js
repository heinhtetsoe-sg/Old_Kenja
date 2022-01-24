// kanji=漢字
function btn_submit(cmd) {
    if (cmd == 'csv') {
        if (document.forms[0].SDATE.value == "" || document.forms[0].EDATE.value == "") {
            alert('指導日付の範囲を指定してください。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}