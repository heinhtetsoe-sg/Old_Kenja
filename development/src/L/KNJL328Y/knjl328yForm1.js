function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

	if (document.forms[0].TESTDIV.value == ""){
		alert("入試区分を指定して下さい");
		return;
	}

    //日付チェック
    ldate = document.forms[0].LOGIN_DATE.value.replace("-","/");
    ldate = ldate.replace("-","/");
	if (document.forms[0].DATE.value < ldate){
		alert("締め切り日付を "+ldate+" 以降に指定してください。");
		return;
	}

    action = document.forms[0].action;
    target = document.forms[0].target;

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
