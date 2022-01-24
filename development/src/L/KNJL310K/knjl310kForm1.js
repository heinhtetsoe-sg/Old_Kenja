function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//NO001
function setdisabled(val) {
    if (val.value == 1){
        document.forms[0].EXAMNOF.disabled = false;
        document.forms[0].EXAMNOT.disabled = false;
        document.forms[0].OUTPUT2[0].disabled = true;//NO002
        document.forms[0].OUTPUT2[1].disabled = true;//NO002
    }else {
        document.forms[0].EXAMNOF.disabled = true;
        document.forms[0].EXAMNOT.disabled = true;
        document.forms[0].OUTPUT2[0].disabled = false;//NO002
        document.forms[0].OUTPUT2[1].disabled = false;//NO002
    }

}
//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

