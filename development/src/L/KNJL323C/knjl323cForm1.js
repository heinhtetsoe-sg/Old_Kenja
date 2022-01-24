function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

	if (document.forms[0].APPLICANTDIV.value == ""){
		alert("入試制度を指定して下さい");
		return;
	}

	if (document.forms[0].TESTDIV.value == ""){
		alert("入試区分を指定して下さい");
		return;
	}

	if (document.forms[0].SHDIV.value == ""){
		alert("専併区分を指定して下さい");
		return;
	}

	if (document.forms[0].isCollegeIgai.value == '1' && document.forms[0].PRINT_TYPE1.checked == false && document.forms[0].PRINT_TYPE2.checked == false && document.forms[0].PRINT_TYPE3.checked == false && document.forms[0].PRINT_TYPE4.checked == false){
		alert("帳票を指定して下さい");
		return;
	}

	if (document.forms[0].isCollegeIgai.value == '' && document.forms[0].PRINT_TYPE1.checked == false && document.forms[0].PRINT_TYPE4.checked == false){
		alert("帳票を指定して下さい");
		return;
	}

	if (document.forms[0].SPECIAL_MEASURES.value == ""){
		alert("追加合格者名簿区分を指定して下さい");
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

