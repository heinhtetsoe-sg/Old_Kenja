function btn_submit(cmd) {
    var date = document.forms[0].DATE.value.replace( /\//g, '-' );
    if (date < document.forms[0].SDATE.value || document.forms[0].EDATE.value < date) {
        alert("年度内の日付を指定してください。");
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
