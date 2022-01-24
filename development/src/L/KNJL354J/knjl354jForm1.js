function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//得点チェック
function score_check(score){

    var s = toInteger(score.value);
    if (score.value == ""){
        score.value = "0";
        score.focus();
        return;
    }else{
        score.value = s;
        return;
    }
}

//印刷
function newwin(SERVLET_URL){
	if (document.forms[0].TESTDIV.value == ""){
		alert("入試区分を指定して下さい");
		return;
	}

	if (document.forms[0].EACH_SCORE.value == ""){
		alert("科目得点率を入力して下さい");
		return;
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
