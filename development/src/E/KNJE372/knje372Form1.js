function btn_submit(cmd) {

    if (cmd == "csv") {
        if (document.forms[0].OUT_DIV[0].checked == true && document.forms[0].QUESTIONNAIRECD.value == "" ){
            alert('{rval MSG304}\n   （調査書名）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){
    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
