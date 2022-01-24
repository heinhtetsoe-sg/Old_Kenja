function btn_submit(cmd) {

    if (!checkInputDate()) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {

    action = document.forms[0].action;
    target = document.forms[0].target;

    if (!checkInputDate()) {
        return false;
    }

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function checkInputDate() {
    //日付入力チェック
    var fromDate = document.forms[0].FROM_DATE.value.split("/").join("-");
    var toDate   = document.forms[0].TO_DATE.value.split("/").join("-");
    if (fromDate == "") {
            alert('開始日付を入力してください。');
            return false;
    } else if (toDate == "") {
            alert('終了日付を入力してください。');
            return false;
    }
    var semesSdate  = document.forms[0].SDATE.value;
    var semesEdate  = document.forms[0].EDATE.value

	if(fromDate > toDate){
		alert('開始日付と終了日付の大小が逆です。');
		return false;
	}

    return true;
}