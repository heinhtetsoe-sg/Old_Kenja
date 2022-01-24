function btn_submit(cmd) {
	//ＣＳＶ出力//2004/07/07 add nakamoto//
	if (cmd == "csv"){
		alert("工事中！！");
		return;
		//チェック
		if ((document.forms[0].KINTAI.checked == false) && (document.forms[0].IDO.checked == false) && 
			(document.forms[0].ALL.checked == false) && (document.forms[0].SEISEKI.checked == false) && 
			(document.forms[0].HORYU.checked == false))
		{
			alert('出力する情報を指定してください。');
			return false;
		}
		if((document.forms[0].ALL.checked == true) && (document.forms[0].DATE.value == ""))
		{
			alert('日付を指定してください。');
			return false;
		}
		if((document.forms[0].HORYU.checked == true) && (document.forms[0].ASSESS.length == 0))
		{
			alert('評価を指定してください。');
			return false;
		}
		if (document.forms[0].GRADE_HR_CLASS.length == 0)
		{
			alert('対象クラスを指定してください。');
			return false;
		}
		if ( (document.forms[0].SEISEKI.checked == true) && (document.forms[0].H_OVER.value == "")  )
		{
			alert('値を指定してください。');
			return false;
		}

	}
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

	if ((document.forms[0].KINTAI.checked == false) && (document.forms[0].IDO.checked == false) && 
		(document.forms[0].ALL.checked == false) && (document.forms[0].SEISEKI.checked == false) && 
		(document.forms[0].HORYU.checked == false))
	{
		alert('出力する情報を指定してください。');
		return false;
	}
	if((document.forms[0].ALL.checked == true) && (document.forms[0].DATE.value == ""))
	{
		alert('日付を指定してください。');
		return false;
	}
	if((document.forms[0].HORYU.checked == true) && (document.forms[0].ASSESS.length == 0))
	{
		alert('評価を指定してください。');
		return false;
	}
	if (document.forms[0].GRADE_HR_CLASS.length == 0)
	{
		alert('対象クラスを指定してください。');
	}

	else if ( (document.forms[0].SEISEKI.checked == true) && (document.forms[0].H_OVER.value == "")  )
	{
		alert('値を指定してください。');
	}

	else
	{
		action = document.forms[0].action;
		target = document.forms[0].target;

	    document.forms[0].action = SERVLET_URL +"/KNJD";
   		document.forms[0].target = "_blank";
		document.forms[0].submit();

		document.forms[0].action = action;
		document.forms[0].target = target;
	}
}

function OverCheck(over)
{
	if(document.forms[0].GAKKI.value == "9")
	{
		if(over.value > 5)
		{
			alert('学年末の評価平均（以上）が、"5"を超えています。');
            over.value = 5;
			over.focus();
			return false;
		}
		else if(over.value < 0 ) {
			alert('学期末の評価平均（以上）が、エラーです。');
            over.value = 0;
			over.focus();
			return false;
		}
	}
	else
	{
		if(over.value > 100)
		{
			alert('学期末の評価平均（以上）が、"100"を超えています。');
            over.value = 100;
			over.focus();
			return false;
		}
		else if(over.value < 0 ) {
			alert('学期末の評価平均（以上）が、エラーです。');
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
			alert('学年末の評価平均（未満）が、"5"を超えています。');
            under.value = 5;
			under.focus();
			return false;
		}
		else if(under.value < 0 ) {
			alert('学期末の評価平均（未満）が、エラーです。');
            under.value = 0;
			under.focus();
			return false;
		}
	}
	else
	{
		if(under.value > 100)
		{
			alert('学期末の評価平均（未満）が、"100"を超えています。');
            under.value = 100;
			under.focus();
			return false;
		}
		else if(under.value < 0 ) {
			alert('学期末の評価平均（未満）が、エラーです。');
            under.value = 0;
			under.focus();
			return false;
		}
	}
}

function ch_seiseki(num1)
{
	if(num1.checked == true)
	{
		flag1=false;
		if(document.forms[0].GAKKI.value == "9")
		{
			document.forms[0].H_OVER.value = 4.3;
			document.forms[0].H_UNDER.value = 1;
		}
		else
		{
			document.forms[0].H_OVER.value = 80;
			document.forms[0].H_UNDER.value = 35;
		}
	}
	else
	{
		flag1=true;
		document.forms[0].H_OVER.value = "";
		document.forms[0].H_UNDER.value = "";
	}
	document.forms[0].H_OVER.disabled = flag1;
	document.forms[0].H_UNDER.disabled = flag1;
}

function ck_horyu(obj1)
{
	if(document.forms[0].HORYU.checked == true)
	{
		flag1=false;
	}
	else
	{
		flag1 = true;
//		document.forms[0].ASSESS.value = "";
	}
	document.forms[0].ASSESS.disabled = flag1;
}

function ch_all(all)
{
	if(all.checked == true)
	{
		flag1=false;
	}
	else
	{
		flag1=true;
	}
	document.forms[0].DATE.disabled = flag1;
	document.forms[0].btn_calen.disabled = flag1;
}

function dis_date(flag)
{
	document.forms[0].DATE.disabled = flag;
	document.forms[0].btn_calen.disabled = flag;
}

