function btn_submit()
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL)
{
    //日付入力チェック
    if (document.forms[0].DATE1.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE1.focus();
        return false;
	}
    if (document.forms[0].DATE2.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE2.focus();
        return false;
	}

    //日付の大小の入れ替え
	date1 = document.forms[0].DATE1;
	date2 = document.forms[0].DATE2;
	change = '';

	if ( date1.value > date2.value )
	{  
		change = date1.value;
		date1.value = date2.value;
		date2.value = change;
	}

    //日付の年度内チェック
    var sdate = document.forms[0].DATE1.value;
    var edate = document.forms[0].DATE2.value;
    var chk_sdate = document.forms[0].CHK_SDATE.value;
    var chk_edate = document.forms[0].CHK_EDATE.value;

    if((sdate < chk_sdate) || (edate > chk_edate)){
        alert("日付が年度範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
