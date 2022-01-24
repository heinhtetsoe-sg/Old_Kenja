function btn_submit(cmd) {

    if (cmd == 'update') {
        if (document.forms[0].TESTDIV.value == '') {
            alert('入試区分を指定して下さい。');
            return true;
        }
        //2:高校のみ
        if (document.forms[0].APPLICANTDIV.value == '2') {
            if (document.forms[0].TESTDIV0.value == '') {
                alert('入試回数を指定して下さい。');
                return true;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
