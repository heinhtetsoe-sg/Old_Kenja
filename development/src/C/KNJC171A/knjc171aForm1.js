function btn_submit()
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL)
{
    //日付入力チェック
    if (document.forms[0].DATE.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE.focus();
        return false;
	}

    //日付の年度内チェック
    var date = document.forms[0].DATE.value;
    var chk_ldate = document.forms[0].CHK_LDATE.value;
    var chk_sdate = document.forms[0].CHK_SDATE.value;
    var chk_edate = document.forms[0].CHK_EDATE.value;

    if((date < chk_sdate) || (date > chk_ldate) || (date > chk_edate)){
        if(chk_ldate > chk_edate){
            alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
        } else {
            alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_ldate + "） ");
        }
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
