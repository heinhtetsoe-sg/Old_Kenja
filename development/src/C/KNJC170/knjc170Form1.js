function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

	if ( document.forms[0].DAYS.value == "" )
	{
		alert('値を指定してください。');
		return false;
	}

	//印刷範囲の大小
//	var irekae = "";
//	var check_from = document.forms[0].NENGETSU_FROM;
//	var check_to   = document.forms[0].NENGETSU_TO;
    
//	if(check_from.value > check_to.value) {
//	    irekae           = check_to.value;
//	    check_to.value   = check_from.value;
//	    check_from.value = irekae;
//	}


    action = document.forms[0].action;
    target = document.forms[0].target;

	url = location.hostname;
	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
	document.forms[0].action = SERVLET_URL +"/KNJC";
	document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function kubun(num1){
	if(num1.value == 1){
		flag1 = false;
	}
	if(num1.value == 2){
		flag1 = true;
	}
	document.forms[0].DAYS.disabled = flag1;
	document.forms[0].NENGETSU_FROM.disabled = flag1;
	document.forms[0].NENGETSU_TO.disabled = flag1;
}

function date_check() {
	var stday = document.forms[0].NENGETSU_FROM.value.split('-');
	var edday = document.forms[0].NENGETSU_TO.value.split('-');
	var chsdy = document.forms[0].NENGETSU_FROM.value;
	var chedy = document.forms[0].NENGETSU_TO.value;

	sdate = stday[0]+stday[1];
	edate = edday[0]+edday[1];
	if( sdate > edate){
		alert('開始日付と終了日付の大小が逆です。');
		document.forms[0].NENGETSU_FROM.value = chedy;
		document.forms[0].NENGETSU_TO.value = chsdy;
		return false;
	}
//	alert(eval(daydt[0]).'<'.eval(sdate[0]));
}

