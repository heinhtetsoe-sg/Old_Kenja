function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//NO002
function setdisabled(obj) {
    if (obj.value == 1){
        document.forms[0].OUTPUT3[0].disabled = false;
        document.forms[0].OUTPUT3[1].disabled = false;
        document.forms[0].OUTPUT4[0].disabled = true;
        document.forms[0].OUTPUT4[1].disabled = true;
    }else {
        document.forms[0].OUTPUT3[0].disabled = true;
        document.forms[0].OUTPUT3[1].disabled = true;
        document.forms[0].OUTPUT4[0].disabled = false;
        document.forms[0].OUTPUT4[1].disabled = false;
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

