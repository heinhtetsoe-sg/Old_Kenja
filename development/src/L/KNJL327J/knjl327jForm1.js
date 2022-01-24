function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function date_check(date){

    var m = toInteger(date.value);
    if (parseInt(m) < 1 || parseInt(m) > 12 || date.value == ""){
        alert("1から12の数字を入力してください。");
        date.value = "";
        date.focus();
        return;
    }else{
        date.value = m;
        return;
    }
}

//印刷
function newwin(SERVLET_URL){
	if (document.forms[0].TESTDIV.value == ""){
		alert("入試区分を指定して下さい");
		return;
	}

	if ((document.forms[0].PRINT_RANGE[1].checked) && 
        (document.forms[0].EXAMNO_FROM.value == "" && document.forms[0].EXAMNO_TO.value == "")){
		alert("受験番号を入力して下さい");
		return;
	}
    
    if ((document.forms[0].PRINT_RANGE[1].checked) && 
        (document.forms[0].EXAMNO_FROM.value != "" && document.forms[0].EXAMNO_TO.value != "")){
        
        from = parseInt(document.forms[0].EXAMNO_FROM.value);
        to = parseInt(document.forms[0].EXAMNO_TO.value);
        
        if (from > to) {
            alert("受験番号の範囲が矛盾しています");
            return;
        }
	}
    
    action = document.forms[0].action;
    target = document.forms[0].target;

	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
