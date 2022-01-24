function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

	if (document.forms[0].YEAR.value == "")
	{
        alert('{rval MSG916}'+'\n　　　( 年度 )');
		document.forms[0].YEAR.focus();
		return;
	}
	if (document.forms[0].PAGE.value < 1)
	{
        alert('{rval MSG916}'+'\n( 印刷開始ページ )');
		document.forms[0].PAGE.focus();
		return;
	}

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJG";
	document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
