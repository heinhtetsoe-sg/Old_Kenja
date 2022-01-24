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

    if ((document.forms[0].CHECK_EXAMNO.value == '3') && (document.forms[0].EXAMNO.value == "")){
        alert("受験番号を入力して下さい");
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

//disabled
function OptionUse(obj)
{
	if(document.forms[0].APPLICANTDIV.value == "1" && document.forms[0].TESTDIV.value == "6")
	{
		document.forms[0].PRINT_TYPE[0].checked = true;
		document.forms[0].PRINT_TYPE[1].disabled = true;
		document.forms[0].PRINT_TYPE[2].disabled = true;
		document.forms[0].CHECK_EXAMNO.value = "1";
		document.forms[0].EXAMNO.disabled = true;
		document.forms[0].EXAMNO.style.backgroundColor = "#cccccc";
	} else {
		document.forms[0].PRINT_TYPE[1].disabled = false;
		document.forms[0].PRINT_TYPE[2].disabled = false;
    }
}
