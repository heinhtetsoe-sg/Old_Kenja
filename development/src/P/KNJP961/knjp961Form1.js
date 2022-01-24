function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    //必須
    if (document.forms[0].PRINT_DIV.value == "1") {
        //伝票番号
        if (document.forms[0].REQUEST_NO.value == "") {
            alert("{rval MSG310}\n（伝票番号）");
            return false;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL + "/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
