function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

	if((document.forms[0].CHECK1.checked == false) && (document.forms[0].CHECK2.checked == false) &&
       (document.forms[0].CHECK3.checked == false) && (document.forms[0].CHECK4.checked == false) &&
       (document.forms[0].CHECK5.checked == false) && (document.forms[0].CHECK6.checked == false))
	{
		alert('{rval MSG916}');
		return false;
	}
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
	document.forms[0].target = "_blank";
	document.forms[0].submit();

	document.forms[0].action = action;
	document.forms[0].target = target;
}

//disabled
function OptionUse(obj) {

    if(document.forms[0].CHECK1.checked == true) {
        document.forms[0].EYESIGHT[0].disabled = false;
        document.forms[0].EYESIGHT[1].disabled = false;
    } else {
        document.forms[0].EYESIGHT[0].disabled = true;
        document.forms[0].EYESIGHT[1].disabled = true;
    }
}
