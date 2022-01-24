function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

	if ( (document.forms[0].YURYO_OVER.value == "") || (document.forms[0].FURYO_S.value == "") || (document.forms[0].KINTAI1_S.value == "") || (document.forms[0].KINTAI2_S.value == "") || (document.forms[0].KINTAI3_S.value == "") )
	{
		alert('値を指定してください。');
		return false;
	}
	if(document.forms[0].DATE.value == "")
	{
		alert('日付を指定してください。');
		return false;
	}

	action = document.forms[0].action;
	target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJD";
   	document.forms[0].target = "_blank";
	document.forms[0].submit();

	document.forms[0].action = action;
	document.forms[0].target = target;
}

function OverCheck(over)
{
	if(document.forms[0].GAKKI.value == "9")
	{
		if(over.value > 5)
		{
			alert('学年末の評定平均（以上）が、"5"を超えています。');
            over.value = 5;
			over.focus();
			return false;
		}
		else if(over.value < 0)
		{
			alert('学年末の評定平均（以上）が、エラーです。');
            over.value = 0;
			over.focus();
			return false;
		}
	}
	else
	{
		if(over.value > 100)
		{
			alert('学期末の評定平均（以上）が、"100"を超えています。');
            over.value = 100;
			over.focus();
			return false;
		}
		else if(over.value < 0)
		{
			alert('学年末の評定平均（以上）が、エラーです。');
            over.value = 0;
			over.focus();
			return false;
		}
	}
}

function UnderCheck(under)
{
	if(document.forms[0].GAKKI.value == "9")
	{
		if(under.value > 5)
		{
			alert('学年末の評定平均（未満）が、"5"を超えています。');
            under.value = 5;
			under.focus();
			return false;
		}
		else if(under.value < 0)
		{
			alert('学年末の評定平均（未満）が、エラーです。');
            under.value = 0;
			under.focus();
			return false;
		}
	}
	else
	{
		if(under.value > 100)
		{
			alert('学期末の評定平均（未満）が、"100"を超えています。');
            under.value = 100;
			under.focus();
			return false;
		}
		else if(under.value < 0)
		{
			alert('学年末の評定平均（未満）が、エラーです。');
            under.value = 0;
			under.focus();
			return false;
		}
	}
}

function Default(def)
{
	if(def.value == "9")
	{
		document.forms[0].YURYO_OVER.value = 4.3;
		document.forms[0].YURYO_UNDER.value = 1;
	}
	else
	{
		document.forms[0].YURYO_OVER.value = 80;
		document.forms[0].YURYO_UNDER.value = 35;
	}
}


function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
