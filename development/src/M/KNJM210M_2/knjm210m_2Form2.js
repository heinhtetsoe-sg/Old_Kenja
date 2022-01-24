function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//àÛç¸
function newwin1(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;
    document.forms[0].PRINTKIND.value = "SCLSUB"

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
	document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//àÛç¸
function newwin2(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;
    document.forms[0].PRINTKIND.value = "REPSUB"

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
	document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
