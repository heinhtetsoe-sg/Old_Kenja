function btn_submit(cmd)
{

	if (cmd == 'delete' && !confirm('{rval MSG103}')){
		return true;
	}
	document.forms[0].cmd.value = cmd;
	document.forms[0].submit();
	return false;
}

function Btn_reset(cmd)
{
	result = confirm('{rval MSG106}');
	if (result == false) {
		return false;
	}
}

function closing_window()
{
		alert('{rval MSG300}');
		closeWin();
		return true;
}

function OnAuthError()
{
	alert('{rval MSG300}');
	closeWin();
}

function isTime(obj)
{
	var timecheck = obj.value;
	// 正規表現による書式チェック
	if (timecheck.match(/^\d{2}\:\d{2}$/) == null && timecheck != "")
	{
		alert("時間の書式が不正です。");
		obj.focus();
		return false;
	}
}

