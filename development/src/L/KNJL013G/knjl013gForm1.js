function btn_submit(cmd) {

    if (cmd == 'update') {
        if (document.forms[0].TESTDIV.value == '') {
            alert('入試区分を指定して下さい。');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
