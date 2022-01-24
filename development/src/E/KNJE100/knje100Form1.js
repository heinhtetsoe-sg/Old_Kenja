function btn_submit(cmd) {
	if (cmd == 'exec'){
		if (!confirm('ＣＳＶデータを取込ます。よろしいですか。'))
			return false;
	}
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError()
{
	alert('{rval MSG300}');
	closeWin();
}