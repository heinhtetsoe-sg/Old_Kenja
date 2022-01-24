function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //日付チェック
    var obj = document.forms[0].DATE;
    if (obj.value == '') {
        alert("日付が不正です。");
        obj.focus();
        return false;
	}

    //処理日
	var d = obj.value.replace(/-/g, '/');
    //学期データ
	var semData = document.forms[0].SEME_DATE.value.split(',');

	var flg = 0;
    for (var i = 0; i < semData.length; i++) {
        var tmpData = semData[i].split(':');

        if (tmpData[1].replace(/-/g, '/') <= d && d <= tmpData[2].replace(/-/g, '/')) {
            flg = tmpData[0];   //学期をセット
        }
    }

	if (!flg) {
		alert("指定範囲が学期外です。");
		return;
	} else {
    	document.forms[0].SEMESTER.value = flg;
	}

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
