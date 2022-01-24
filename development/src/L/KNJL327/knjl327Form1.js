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
	if (document.forms[0].TESTDV.value == ""){
		alert("入試区分を指定して下さい");
		return;
	}
	if (document.forms[0].NOTICEDAY.value == ""){
		alert("通知日付を入力して下さい");
		return;
	}
	if (((document.forms[0].OUT.value == 1)||(document.forms[0].OUT.value == 5))&&
		(document.forms[0].OUTA.value == 2)&&(document.forms[0].EXAMNOA.value == "")){
		alert("受験番号を入力して下さい");
		return;
	}
	if ((document.forms[0].OUT.value == 6)&&(document.forms[0].OUTB.value == 2)&&
		(document.forms[0].EXAMNOB.value == "")){
		alert("受験番号を入力して下さい");
		return;
	}
	if (document.forms[0].OUT.value == 6){
		if(document.forms[0].DEADLINE.value == ""){
			alert("提出日付を入力して下さい");
			return;
		} else if((document.forms[0].TIMEUPH.value == "")||(document.forms[0].TIMEUPM.value == "")){
			alert("提出時間を入力して下さい");
			return;
		}
	}
	if (((document.forms[0].OUT.value == 2)||(document.forms[0].OUT.value == 3))&&
		(document.forms[0].OUTC.value == 2)&&(document.forms[0].EXAMNOC.value == "")){
		alert("受験番号を入力して下さい");
		return;
	}
	if (((document.forms[0].OUT.value == 7)||(document.forms[0].OUT.value == 8))&&
		(document.forms[0].OUTE.value == 2)&&(document.forms[0].EXAMNOE.value == "")){
		alert("受験番号を入力して下さい");
		return;
	}
	if ((document.forms[0].OUT.value == 4)&&(document.forms[0].OUTD.value == 2)&&
		(document.forms[0].EXAMNOD.value == "")){
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
