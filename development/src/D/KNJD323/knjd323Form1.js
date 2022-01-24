function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

	if (document.forms[0].GRADE.value == '')
	{
		alert("学年を選択して下さい。");
		return;
	}
	if (document.forms[0].DATE.value == '')
	{
		alert("異動対象日付が未入力です。");
		return;
	}

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
	document.forms[0].action = SERVLET_URL +"/KNJD";
	document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

