function btn_submit(cmd) {
    if (cmd == 'csv') {
        if (document.forms[0].APPLICANTDIV.value == ""){
            alert("入試制度を指定して下さい");
            return;
        }
        if (document.forms[0].TESTDIV.value == ""){
            alert("入試区分を指定して下さい");
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
