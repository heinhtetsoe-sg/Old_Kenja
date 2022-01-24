function btn_submit()
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL)
{
    //日付のチェック
    var date_from = document.forms[0].DATE_FROM;
    var date_to   = document.forms[0].DATE_TO;
    var chk_sdate = document.forms[0].CHK_SDATE.value;
    var chk_edate = document.forms[0].CHK_EDATE.value;
    var irekae    = "";

    //入力チェック
    if (date_from.value == "") {
        alert("開始日付が未入力です。");
        date_from.focus();
        return false;
	}
    if (date_to.value == "") {
        alert("終了日付が未入力です。");
        date_to.focus();
        return false;
	}

    //大小チェック
    if(date_from.value > date_to.value){
	    irekae          = date_to.value;
	    date_to.value   = date_from.value;
	    date_from.value = irekae;
    }

    //日付の年度内チェック
    if((date_from.value < chk_sdate) || (date_to.value > chk_edate)){
        alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
        if(date_from.value < chk_sdate){
            date_from.focus();
        } else {
            date_to.focus();
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
