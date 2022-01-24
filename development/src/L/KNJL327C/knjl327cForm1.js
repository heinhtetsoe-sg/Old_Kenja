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
    if (document.forms[0].PRINT_DATE.value == "") {
        alert("通知日付が未入力です。");
        return;
    }
    if ((document.forms[0].CHECK_OUTPUT.value == '5') && (document.forms[0].VISIT_DATE.value == "")) {
        alert("来校日付が未入力です。");
        return;
    }
    if ((document.forms[0].CHECK_OUTPUT.value == '5') && (document.forms[0].VISIT_HOUR.value == "")) {
        alert("来校時間（時）が未入力です。");
        return;
    }
    if ((document.forms[0].CHECK_OUTPUT.value == '5') && (document.forms[0].VISIT_MINUTE.value == "")) {
        alert("来校時間（分）が未入力です。");
        return;
    }
    if ((document.forms[0].CHECK_OUTPUT.value == '1') && (document.forms[0].CHECK_OUTPUTA.value == '3') &&
        (document.forms[0].EXAMNOA.value == "")) {
        alert("受験番号を入力して下さい");
        return;
    }
    if ((document.forms[0].CHECK_OUTPUT.value == '2') && (document.forms[0].CHECK_OUTPUTD.value == '3') &&
        (document.forms[0].EXAMNOD.value == "")) {
        alert("受験番号を入力して下さい");
        return;
    }
    if ((document.forms[0].CHECK_OUTPUT.value == '3') && (document.forms[0].CHECK_OUTPUTB.value == '2') &&
        (document.forms[0].EXAMNOB.value == "")) {
        alert("受験番号を入力して下さい");
        return;
    }
    if ((document.forms[0].CHECK_OUTPUT.value == '4') && (document.forms[0].CHECK_OUTPUTC.value == '2') &&
        (document.forms[0].EXAMNOC.value == "")) {
        alert("受験番号を入力して下さい");
        return;
    }
    if ((document.forms[0].CHECK_OUTPUT.value == '5') && (document.forms[0].CHECK_OUTPUTE.value == '2') &&
        (document.forms[0].EXAMNOE.value == "")) {
        alert("受験番号を入力して下さい");
        return;
    }
    if (document.forms[0].CHECK_OUTPUT.value == '6') {
        if (document.forms[0].CHECK_OUTPUTF.value == '2' && document.forms[0].EXAMNOF.value == "") {
            alert("受験番号を入力して下さい");
            return;
        }
        if (document.forms[0].SINGAKU_DATE.value == "") {
            alert("入学日付が未入力です。");
            return;
        }
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

//来校時間チェック
function TimeCheck(div, time)
{
    if(div == 'hour'){
        if(time.value > 24 || time.value < 0){
            alert("来校時間(時)が範囲外です。");
            time.focus();
            return false;
        }
    } else {
        if(time.value > 59 || time.value < 0){
            alert("来校時間(分)が範囲外です。");
            time.focus();
            return false;
        }
    }

}

