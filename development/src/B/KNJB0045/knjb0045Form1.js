function btn_submit(cmd) {
    if (cmd == 'execute') {
		if (document.forms[0].RADIO[0].checked == true)	{
			if　(document.forms[0].TITLE.length == 0) {
				return;
			}
			if (document.forms[0].DATE.value == "") {
				alert("名簿の日付を指定して下さい。");
	    	    return;
			}
		}
		if (document.forms[0].RADIO[1].checked == true)	{
			if (document.forms[0].DATE_FROM.value == "" || document.forms[0].DATE_TO.value == "") {
				alert("日付を指定して下さい。");
	    	    return;
			}
		}
	    if (document.forms[0].OPERATION.value == "") {
			alert("教師稼動数を指定して下さい。");
	        return;
		}

	    if (!confirm('{rval MSG101}')) {
    	    return false;
	    }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//チェック実行（バッチ処理）
function chk_submit(SERVLET_URL)
{
    document.forms[0].PRGID.value = "KNJB0045B";//バッチ処理用Servlet

    action = document.forms[0].action;
    target = document.forms[0].target;

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//帳票印刷
function newwin(SERVLET_URL)
{
    document.forms[0].PRGID.value = "KNJB0045P";//印刷用Servlet

    action = document.forms[0].action;
    target = document.forms[0].target;

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//通常・基本項目の使用可・不可
function jikanwari(num){
	if(num == 1){
		flag1 = false;
		flag2 = true;
	} else {
		flag1 = true;
		flag2 = false;
	}
	document.forms[0].NENDO.disabled 		= flag1;
	document.forms[0].TITLE.disabled 		= flag1;
	document.forms[0].DATE_FROM.disabled  	= flag2;
	document.forms[0].DATE_TO.disabled  	= flag2;
	document.forms[0].DATE.disabled  		= flag1;
    for (var i=0;i<document.forms[0].elements.length;i++) {
        var e = document.forms[0].elements[i];
        if (e.type=='button' && e.name=='btn_calen'){
       	    if (i==4) 			e.disabled = flag1;
       	    if (i==7 || i==9) 	e.disabled = flag2;
   	    }
    }
}
