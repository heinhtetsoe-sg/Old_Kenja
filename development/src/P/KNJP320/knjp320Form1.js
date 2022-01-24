function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function outcheck(out) {
	if (out.value == '4'){
		document.forms[0].element[2].disabled = true;
		document.forms[0].element[3].disabled = true;
	}else {
		document.forms[0].element[2].disabled = false;
		document.forms[0].element[3].disabled = false;
	}
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
	document.forms[0].action = SERVLET_URL +"/KNJP";
   	document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}


