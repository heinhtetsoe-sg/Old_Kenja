function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function time_check(time){
    switch(time.name){
        case "TIMEUPH":
            var h = toInteger(time.value);
            if (parseInt(h) < 0 || parseInt(h) > 23){
                alert("０から２３の数字を入力してください。");
                time.focus();
            }else{
                time.value = h;
            }
            break;
        case "TIMEUPM":
            var m = toInteger(time.value);
            if (parseInt(m) < 0 || parseInt(m) > 59){
                alert("０から５９の数字を入力してください。");
                time.focus();
            }else{
                time.value = m;
            }
            break;
		default :
            break;
    }
}

//印刷
function newwin(SERVLET_URL){
	if (document.forms[0].RECEIPT_DATE.value == ""){
		alert("受付日付を入力して下さい");
		return;
	}
    action = document.forms[0].action;
    target = document.forms[0].target;

	url = location.hostname;
	//document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
