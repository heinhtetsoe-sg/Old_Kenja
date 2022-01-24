function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){ 
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
    
    if (document.forms[0].DOMI_CD.value == "") {
        alert('{rval MSG301}'+' 寮コードが未設定です。');
        return false;
    }
    if (document.forms[0].DOMI_ENTDAY.value == "") {
        alert('{rval MSG301}'+' 日付が未設定です。');
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//セキュリティチェック
function OnAuthError()
{
	alert('{rval MSG300}');
	closeWin();
}
