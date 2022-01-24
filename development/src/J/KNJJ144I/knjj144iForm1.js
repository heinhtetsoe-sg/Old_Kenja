function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function retfalse() {
    return false;
}

function setGroupDiv() {
}

function chkNotPrintLastExam(chk) {
    if (chk.checked) {
        if (chk == document.forms[0].NOT_PRINT_LASTEXAM) {
            document.forms[0].NOT_PRINT_LASTEXAM_SCORE.checked = false;
        } else if (chk == document.forms[0].NOT_PRINT_LASTEXAM_SCORE) {
            document.forms[0].NOT_PRINT_LASTEXAM.checked = false;
        }
    }
}

function newwin(SERVLET_URL){
    
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJJ";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
