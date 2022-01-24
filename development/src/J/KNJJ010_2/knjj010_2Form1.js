function btn_submit(cmd) {
    
	if (cmd == 'delete'){
		if (!confirm('{rval MB1003}'))
			return false;
	}
	document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function EnableBtns(e){

	document.forms[0].btn_add.disabled    = false;
	document.forms[0].btn_udpate.disabled = false;
	document.forms[0].btn_reset.disabled  = false;

}

function ShowConfirm(){
	if (!confirm('{rval MZ0003}'))
		return false;
}

function OnAuthError()
{
	alert('{rval MZ0026}');
	closeWin();
}