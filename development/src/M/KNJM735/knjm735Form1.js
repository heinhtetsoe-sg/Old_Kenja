function btn_submit(cmd) {
    if (cmd == "update") {
        if (document.forms[0].PAID_MONEY_DATE.value == "") {
            alert('入金日を指定して下さい。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
