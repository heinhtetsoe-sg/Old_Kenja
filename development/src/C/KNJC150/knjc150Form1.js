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
    var irekae = "";
    var check_from = document.forms[0].NENGETSU_FROM;
    var check_to   = document.forms[0].NENGETSU_TO;
    
    if(check_from.value > check_to.value) {
	    irekae           = check_to.value;
	    check_to.value   = check_from.value;
	    check_from.value = irekae;
	}


    action = document.forms[0].action;
    target = document.forms[0].target;

	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
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
	var kaisi = document.forms[0].sday.value.split('-');
	var owari = document.forms[0].eday.value.split('-');
	var stday = document.forms[0].NENGETSU_FROM.value.split('-');
	var edday = document.forms[0].NENGETSU_TO.value.split('-');

	semsd = kaisi[0]+kaisi[1];
	semed = owari[0]+owari[1];
	sdate = stday[0]+stday[1];
	edate = edday[0]+edday[1];
	if(semsd > sdate || sdate > semed){
		alert('開始日付が学期範囲外です。');
		document.forms[0].NENGETSU_FROM.value = document.forms[0].sday.value;
		return false;
	}
	if(semsd > edate || edate > semed){
		alert('終了日付が学期範囲外です。');
		document.forms[0].NENGETSU_TO.value = document.forms[0].eday.value;
		return false;
	}
	if( sdate > edate){
		alert('開始日付と終了日付の大小が逆です。');
		document.forms[0].NENGETSU_FROM.value = document.forms[0].sday.value;
		document.forms[0].NENGETSU_TO.value = document.forms[0].eday.value;
		return false;
	}
//	alert(eval(daydt[0]).'<'.eval(sdate[0]));
}

