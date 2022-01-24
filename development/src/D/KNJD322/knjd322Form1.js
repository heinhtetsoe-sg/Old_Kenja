function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){
/*
	var subclass = document.forms[0].SUBCLASS;//欠点科目数
	var output1 = document.forms[0].OUTPUT[0];//以上
	var output2 = document.forms[0].OUTPUT[1];//未満

	if (subclass.value == "")
	{
		alert('値を指定してください。');
		return false;
	}

	if (output1.checked && subclass.value < 1)
	{
		alert('０以外の値を指定してください。');
		return false;
	}

	if (output2.checked && subclass.value < 2)
	{
		alert('０,１以外の値を指定してください。');
		return false;
	}
*/
	//チェック
	if (document.forms[0].GRADE.length == 0)
	{
		alert('{rval MSG916}');
		return;
	}

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

