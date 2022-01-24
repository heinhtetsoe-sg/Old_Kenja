function btn_submit(cmd) {

    if (cmd == "csv") {
        if (document.forms[0].PAID_MONEY_DATE.value == "") {
            alert('引落日を指定して下さい。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
